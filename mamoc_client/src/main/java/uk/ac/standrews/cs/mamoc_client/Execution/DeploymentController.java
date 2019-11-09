package uk.ac.standrews.cs.mamoc_client.Execution;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Subscription;
import java8.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.mamoc_client.DecisionMaker.NodeOffloadingPercentage;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.standrews.cs.mamoc_client.Model.Task;

import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_PUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_RESULT_SUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.SENDING_FILE_PUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class DeploymentController {

    private final String TAG = "DeploymentController";

    private Context mContext;

    private static DeploymentController instance;

    private MamocFramework framework;

    private Subscription sub;
    private long startSendingTime, endSendingTime;

    private Task task;

    File outputResults;
    private boolean subscribed;


    private DeploymentController(Context context) {
        this.mContext = context;
        framework = MamocFramework.getInstance(context);
        outputResults = new File(getOutputDir(context), "output.txt");
    }

    public static DeploymentController getInstance(Context context) {
        if (instance == null) {
            synchronized (DeploymentController.class) {
                if (instance == null) {
                    instance = new DeploymentController(context);
                }
            }
        }
        return instance;
    }

    public void runLocally(Task t, String resource_name, Object... params) {
        Log.d(TAG, "running " + t.getTaskName() + " locally");

        task = new Task();
        task.setTaskName(t.getTaskName());
        task.setExecLocation(ExecutionLocation.LOCAL);
        task.setCommOverhead(0.0);
        task.setNetworkType(framework.networkProfiler.getNetworkType());
        task.setExecutionDate(System.currentTimeMillis());

        startSendingTime = System.nanoTime();

        try {
            Class<?> cls = Class.forName(task.getTaskName());
            Constructor<?> constructor;
            Object instance;

            if (resource_name != null){
                String fileContent = getContentFromTextFile(resource_name);

                // create a new parameters array with the file content appended to it
                Object[] newParams = new Object[params.length + 1];
                newParams[0] = fileContent;
                System.arraycopy(params, 0, newParams, 1, params.length);

                constructor = getAppropriateConstructor(cls, newParams);
                instance = constructor.newInstance(newParams);
            } else {
                constructor = getAppropriateConstructor(cls, params);
                instance = constructor.newInstance(params);
            }

            Method runMethod = instance.getClass().getMethod("run");

            Object result = runMethod.invoke(instance);

            // if nothing is returned from the execution
            if (result == null){
                result = "Nothing";
            }

            endSendingTime = System.nanoTime();
            double executionTime = (double)(endSendingTime - startSendingTime) * 1.0e-9;

            broadcastLocalResults(result, executionTime);

            task.setExecutionTime(executionTime);
            addExecutionEntry(task);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void executeRemotely(Context context, ExecutionLocation location, Task t, String resource_name, Object... params) {

        Log.d(TAG, "running " + t.getTaskName() + " remotely");

        task = new Task();
        task.setTaskName(t.getTaskName());
        task.setExecLocation(location);
        task.setNetworkType(framework.networkProfiler.getNetworkType());
        task.setExecutionDate(System.currentTimeMillis());

        switch (location) {

            case D2D:
                runNearby(context, resource_name, params);
                task.setExecLocation(ExecutionLocation.D2D);
                break;

            case EDGE:
                runOnEdge(context, resource_name, params);
                task.setExecLocation(ExecutionLocation.EDGE);
                break;

            case PUBLIC_CLOUD:
                runOnCloud(context, resource_name, params);
                task.setExecLocation(ExecutionLocation.PUBLIC_CLOUD);
                break;
        }
    }

    public void runDynamically(Context context, Task t, String resource_name, Object... params) {

        Log.d(TAG, "running " + t.getTaskName() + " dynamically");

        ArrayList<NodeOffloadingPercentage> nodeOffPerc = framework.decisionEngine.makeDecision(t, false, 0.5, 0.5);

        if (nodeOffPerc.size() == 1 && nodeOffPerc.get(0).getNode() == framework.getSelfNode()){
            runLocally(t, resource_name, params);
        } else {
            if (nodeOffPerc.get(0).getNode() instanceof MobileNode) {
                executeRemotely(context, ExecutionLocation.D2D, t, resource_name, params);
            } else if (nodeOffPerc.get(0).getNode() instanceof EdgeNode) {
                executeRemotely(context, ExecutionLocation.EDGE, t, resource_name, params);
            } else if (nodeOffPerc.get(0).getNode() instanceof CloudNode) {
                executeRemotely(context, ExecutionLocation.PUBLIC_CLOUD, t, resource_name, params);
            }
            // This should not happen!
            else {
                runLocally(t, resource_name, params);
            }
        }
    }

    private void runNearby(Context context, String resource_name, Object... params) {

        Log.d(TAG, "running " + task.getTaskName() + " nearby");

        TreeSet<MobileNode> mobileNodes = framework.serviceDiscovery.listMobileNodes();

        // TODO: Java Reflect dynamic call to class on connected mobile nodes

    }

    private void runOnEdge(Context context, String resource_name, Object... params){

        Log.d(TAG, "running " + task.getTaskName() + " on edge");

        TreeSet<EdgeNode> edgeNodes = framework.serviceDiscovery.listEdgeNodes();
        if (!edgeNodes.isEmpty()) {
            EdgeNode node = edgeNodes.first(); // for now we assume we are connected to one edge device
//            task.setRttSpeed(framework.networkProfiler.measureRtt(node.getIp(), node.getPort()));
//            Log.d(TAG, "RTT SPEED TO EDGE: " + framework.networkProfiler.measureRtt(node.getIp(), node.getPort()));
            executeRemotely(context, node, resource_name, params);
        } else {
            Toast.makeText(context, "No edge node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOnCloud(Context context, String resource_name, Object... params){

        Log.d(TAG, "running " + task.getTaskName() + " on public cloud");

        TreeSet<CloudNode> cloudNodes = framework.serviceDiscovery.listPublicNodes();
        if (!cloudNodes.isEmpty()) {
            CloudNode node = cloudNodes.first();
            executeRemotely(context, node, resource_name, params);
        } else {
            Toast.makeText(context, "No cloud node exists", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Execute the task on the Edge node
     * @param context
     * @param node Edge node
     * @param resource_name The resource name if any
     * @param params Other params
     */
    private void executeRemotely(Context context, EdgeNode node, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  task.getTaskName() + " procedure");

            mContext = context;

            startSendingTime = System.nanoTime();

            // subscribe to the result of offloading
                CompletableFuture<Subscription> subFuture = node.session.subscribe(
                        OFFLOADING_RESULT_SUB,
                        this::onOffloadingResult);

                subFuture.whenComplete((subscription, throwable) -> {
                    if (throwable == null) {

                        mContext = context;
                        sub = subscription;
                        subscribed = true;
                        // We have successfully subscribed.
                        Log.d(TAG, "Subscribed to topic " + subscription.topic);
                    } else {
                        // Something went bad.
                        throwable.printStackTrace();
                        task.setCompleted(false);
                        addExecutionEntry(task);
                    }
                });

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, task.getTaskName());

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, task.getTaskName() + " not registered");
                    Toast.makeText(context, task.getTaskName() + " not registered", Toast.LENGTH_SHORT).show();

                    try {

                        // publish the dependent resource file for the task
//                        if (resource_name != null) {
//                            // publish the necessary resource files
//                            CompletableFuture<Publication> pubFuture = node.session.publish(
//                                    SENDING_FILE_PUB,
//                                    "Android",
//                                    resource_name,
//                                    readFromAssets(mContext, resource_name));
//                            pubFuture.thenAccept(publication -> Log.d("publishResult",
//                                    resource_name + " published successfully"));
//                            // Shows we can separate out exception handling
//                            pubFuture.exceptionally(throwable -> {
//                                Log.e(TAG, "Failed to publish resource file");
//                                throwable.printStackTrace();
//                                return null;
//                            });
//                        }

                        String sourceCode = framework.fetchSourceCode(task.getTaskName());

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                task.getTaskName(),
                                sourceCode,
                                resource_name,
                                params);
                        pubFuture.thenAccept(publication -> Log.d("publishResult",
                                task.getTaskName() + " source code published successfully"));
                        // Shows we can separate out exception handling
                        pubFuture.exceptionally(throwable -> {
                            throwable.printStackTrace();
                            task.setCompleted(false);
                            return null;
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        task.setCompleted(false);
                    }
                } else {
                    // Call the task procedure.
                    Log.d(TAG, String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture;

                    // We will handle the resource name being null in mamoc server to keep the RPC calls standard across different tasks
//                    if (resource_name != null) {

                    try {
                        node.session.call(task.getTaskName(), resource_name, params);
                    } catch (Exception e) {
                        Log.e(TAG, "exception in RPC call: " + e.getMessage());
                        task.setCompleted(false);
                    }
//                    } else{
//                        callFuture = node.session.call(
//                                task.getTaskName(), params);
//                    }

//                    callFuture.thenAccept(callResult -> {
//                        List<Object> results = (List) callResult.results.get(0);
//
//                        broadcastResults(results);
//                    });
                }
            });
        } else {
            Toast.makeText(context, "Edge is not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Runs the task on the Cloud Node
     * @param context
     * @param node Cloud node
     * @param resource_name resource name if any
     * @param params extra paramaters
     */
    private void executeRemotely(Context context, CloudNode node, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  task.getTaskName() + " procedure");

            mContext = context;

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, task.getTaskName());

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, task.getTaskName() + " not registered");
                    Toast.makeText(context, task.getTaskName() + " not registered", Toast.LENGTH_SHORT).show();

                    try {
                        // subscribe to the result of offloading
                        CompletableFuture<Subscription> subFuture = node.session.subscribe(
                                OFFLOADING_RESULT_SUB,
                                this::onOffloadingResult);

                        subFuture.whenComplete((subscription, throwable) -> {
                            if (throwable == null) {
                                mContext = context;
                                sub = subscription;
                                // We have successfully subscribed.
                                Log.d(TAG, "Subscribed to topic " + subscription.topic);
                            } else {
                                // Something went bad.
                                throwable.printStackTrace();
                            }
                        });

                        startSendingTime = System.nanoTime();

                        // publish the dependent resource file for the task
                        if (resource_name != null) {
                            // publish the necessary resource files
                            CompletableFuture<Publication> pubFuture = node.session.publish(
                                    SENDING_FILE_PUB,
                                    "Android",
                                    resource_name,
                                    "this is file content");
                            pubFuture.thenAccept(publication -> Log.d("publishResult",
                                    "Published successfully"));
                            // Shows we can separate out exception handling
                            pubFuture.exceptionally(throwable -> {
                                throwable.printStackTrace();
                                return null;
                            });
                        }

                        String sourceCode =  framework.fetchSourceCode(task.getTaskName());

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                task.getTaskName(),
                                sourceCode,
                                resource_name,
                                params);
                        pubFuture.thenAccept(publication -> Log.d("publishResult",
                                "Published successfully"));
                        // Shows we can separate out exception handling
                        pubFuture.exceptionally(throwable -> {
                            throwable.printStackTrace();
                            return null;
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Call the task procedure.
                    Log.d(TAG, String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture;

                    if (resource_name != null) {
                        callFuture = node.session.call(
                                task.getTaskName(), resource_name, params);
                    } else{
                        callFuture = node.session.call(
                                task.getTaskName(), params);
                    }

//                    callFuture.thenAccept(callResult -> {
//                        List<Object> results = (List) callResult.results.get(0);
//
//                        broadcastResults(results);
//                    });
                }
            });
        } else {
            Toast.makeText(context, "Edge is not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onOffloadingResult(List<Object> results) {
        endSendingTime = System.nanoTime();
        broadcastResults(results);
        sub.unsubscribe();
    }

    private void broadcastResults(List<Object> results){
        String executionResult = String.valueOf(results.get(0));
        double executionTime = (Double) results.get(1);

        Log.d(TAG, "Received result: " + executionResult + " in " + executionTime + " secs");
        
        Log.d(TAG, "start in ns: " + startSendingTime);
        Log.d(TAG, "end in ns: " + endSendingTime);

        double totalTime = (double)(endSendingTime - startSendingTime) * 1.0e-9;

        Log.d(TAG, "Total time in s: " + totalTime);

        double commOverhead = totalTime - executionTime;

        Log.d(TAG, "Comm overhead: " + commOverhead);

        Log.d(TAG, "Broadcasting offloading result");

        Intent intent = new Intent(OFFLOADING_RESULT_SUB);
        intent.putExtra("result", executionResult);
        intent.putExtra("duration", executionTime);
        intent.putExtra("overhead", commOverhead);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        task.setExecutionTime(executionTime);
        task.setCommOverhead(commOverhead);
        task.setCompleted(true);

        Log.d(TAG, "insert it: " + task.getTaskName());

        addExecutionEntry(task);
    }

    private void addExecutionEntry(Task task){
        Log.d(TAG, "addExecutionEntry called");

        long insertResult = framework.dbAdapter.addTaskExecution(task);
        if (insertResult != -1){
            Log.d(TAG, "inserted " + task.getTaskName());
        } else {
            Log.e(TAG, "failed to insert " + task.getTaskName());
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputResults, true)));
            out.println(task.getExecLocation().getValue() + " " + task.getExecutionTime());
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "Save file error", e);
        }
    }

    private void broadcastLocalResults(Object result, double duration){

        Log.d(TAG, "Broadcasting local result");
        Intent intent = new Intent(OFFLOADING_RESULT_SUB);
        intent.putExtra("result", String.valueOf(result));
        intent.putExtra("duration", duration);
        intent.putExtra("overhead", 0.0);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


    // https://stackoverflow.com/a/18136892/1478212
    private static <C> Constructor<C> getAppropriateConstructor(Class<C> c, Object[] initArgs){
        if(initArgs == null)
            initArgs = new Object[0];
        for(Constructor con : c.getDeclaredConstructors()){
            Class[] types = con.getParameterTypes();
            if(types.length!=initArgs.length)
                continue;
            boolean match = true;
            for(int i = 0; i < types.length; i++){
                Class need = types[i], got = initArgs[i].getClass();
                if(!need.isAssignableFrom(got)){
                    if(need.isPrimitive()){
                        match = (int.class.equals(need) && Integer.class.equals(got))
                                || (long.class.equals(need) && Long.class.equals(got))
                                || (char.class.equals(need) && Character.class.equals(got))
                                || (short.class.equals(need) && Short.class.equals(got))
                                || (boolean.class.equals(need) && Boolean.class.equals(got))
                                || (byte.class.equals(need) && Byte.class.equals(got));
                    }else{
                        match = false;
                    }
                }
                if(!match)
                    break;
            }
            if(match)
                return con;
        }
        throw new IllegalArgumentException("Cannot find an appropriate constructor for class " + c + " and arguments " + Arrays.toString(initArgs));
    }

    private String getContentFromTextFile(String file) {

        String fileContent = null;
        try {
            fileContent = readFromAssets(mContext, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    private String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    private File getOutputDir(Context context){

        String ExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        String folder_main ="mamoc";

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }

        Log.d("externalstorage", ExternalStoragePath);

        try{
            return new File(ExternalStoragePath  + "/" + folder_main);
        } catch (Throwable x) {
            Log.e(TAG, "could not create an output directory");
        }

        return null;
    }

}
