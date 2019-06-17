package uk.ac.standrews.cs.mamoc_client.Execution;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Subscription;
import java8.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Model.TaskExecution;

import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_PUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_RESULT_SUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class ExecutionController {

    private final String TAG = "ExecutionController";

    private Context mContext;

    private static ExecutionController instance;

    private MamocFramework framework;

    private Subscription sub;
    private long startSendingTime, endSendingTime;

    private TaskExecution task;

    private ExecutionController(Context context) {
        this.mContext = context;
        framework = MamocFramework.getInstance(context);
    }

    public static ExecutionController getInstance(Context context) {
        if (instance == null) {
            synchronized (ExecutionController.class) {
                if (instance == null) {
                    instance = new ExecutionController(context);
                }
            }
        }
        return instance;
    }

    public void runLocally(String task_name, String resource_name, Object... params) {
        Log.d(TAG, "running " + task_name + " locally");

        task = new TaskExecution();
        task.setTaskName(task_name);
        task.setExecLocation(ExecutionLocation.LOCAL);
        task.setCommOverhead(0.0);
        task.setNetworkType(framework.networkProfiler.getNetworkType());
        task.setExecutionDate(System.currentTimeMillis());

        startSendingTime = System.nanoTime();

        try {
            Class<?> cls = Class.forName(task_name);
            Constructor<?> constructor;
            Object instance;

            if (!resource_name.equalsIgnoreCase("None")){
                String fileContent = getContentFromTextFile(resource_name + ".txt");

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
            task.setExecutionTime(executionTime);
            addExecutionEntry(task);
            broadcastLocalResults(result, executionTime);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void runRemotely(Context context, ExecutionLocation location, String task_name, String resource_name, Object... params) {

        Log.d(TAG, "running " + task_name + " remotely");

        task = new TaskExecution();
        task.setTaskName(task_name);
        task.setNetworkType(framework.networkProfiler.getNetworkType());
        task.setExecutionDate(System.currentTimeMillis());

        switch (location) {

            case D2D:
                runNearby(context, task_name, resource_name, params);
                task.setExecLocation(ExecutionLocation.D2D);
                break;

            case EDGE:
                runOnEdge(context, task_name, resource_name, params);
                task.setExecLocation(ExecutionLocation.EDGE);
                break;

            case PUBLIC_CLOUD:
                runOnCloud(context, task_name, resource_name, params);
                task.setExecLocation(ExecutionLocation.PUBLIC_CLOUD);
                break;
        }
    }

    public void runDynamically(Context context, String task_name, String resource_name, Object... params) {

        Log.d(TAG, "running " + task_name + " dynamically");

        ExecutionLocation location = framework.decisionEngine.makeDecision(task_name, false);

        if (location == ExecutionLocation.LOCAL) {
            runLocally(task_name, resource_name, params);
        } else {
            runRemotely(context, location, task_name, resource_name, params);
        }
    }

    private void runNearby(Context context, String task_name, String resource_name, Object... params) {

        Log.d(TAG, "running " + task_name + " nearby");

        // TODO: Java Reflect dynamic call to class on connected mobile nodes

    }

    private void runOnEdge(Context context, String task_name, String resource_name, Object... params){

        Log.d(TAG, "running " + task_name + " on edge");

        TreeSet<EdgeNode> edgeNodes = framework.commController.getEdgeDevices();
        if (!edgeNodes.isEmpty()) {
            EdgeNode node = edgeNodes.first(); // for now we assume we are connected to one edge device
//            task.setRttSpeed(framework.networkProfiler.measureRtt(node.getIp(), node.getPort()));
//            Log.d(TAG, "RTT SPEED TO EDGE: " + framework.networkProfiler.measureRtt(node.getIp(), node.getPort()));
            runRemotely(context, node, task_name, resource_name, params);
        } else {
            Toast.makeText(context, "No edge node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOnCloud(Context context, String task_name, String resource_name, Object... params){

        Log.d(TAG, "running " + task_name + " on public cloud");

        TreeSet<CloudNode> cloudNodes = framework.commController.getCloudDevices();
        if (!cloudNodes.isEmpty()) {
            CloudNode node = cloudNodes.first();
            runRemotely(context, node, task_name, resource_name, params);
        } else {
            Toast.makeText(context, "No cloud node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runRemotely(Context context, EdgeNode node, String task_name, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  task_name + " procedure");

            mContext = context;

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, task_name);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, task_name + " not registered");
                    Toast.makeText(context, task_name + " not registered", Toast.LENGTH_SHORT).show();

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
                                task.setCompleted(false);
                                addExecutionEntry(task);
                            }
                        });

                        String sourceCode = framework.fetchSourceCode(task_name);

                        startSendingTime = System.nanoTime();

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                task_name,
                                sourceCode,
                                resource_name,
                                params);
                        pubFuture.thenAccept(publication -> Log.d("publishResult",
                                "Published successfully"));
                        // Shows we can separate out exception handling
                        pubFuture.exceptionally(throwable -> {
                            throwable.printStackTrace();
                            task.setCompleted(false);
                            addExecutionEntry(task);
                            return null;
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        task.setCompleted(false);
                        addExecutionEntry(task);
                    }
                } else {
                    // Call the task procedure.
                    Log.d(TAG, String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            task_name, params);
                    callFuture.thenAccept(callResult -> {
                        List<Object> results = (List) callResult.results.get(0);

                        broadcastResults(results);
                    });
                }
            });
        } else {
            Toast.makeText(context, "Edge is not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void runRemotely(Context context, CloudNode node, String task_name, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  task_name + " procedure");

            mContext = context;

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, task_name);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, task_name + " not registered");
                    Toast.makeText(context, task_name + " not registered", Toast.LENGTH_SHORT).show();

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

                        String sourceCode =  framework.fetchSourceCode(task_name);

                        startSendingTime = System.nanoTime();

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                task_name,
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

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            task_name, params);
                    callFuture.thenAccept(callResult -> {
                        List<Object> results = (List) callResult.results.get(0);

                        broadcastResults(results);
                    });
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

        double totalTime = (double)(endSendingTime - startSendingTime) * 1.0e-9;
        double commOverhead = totalTime - executionTime;

        task.setExecutionTime(executionTime);
        task.setCommOverhead(commOverhead);
        task.setCompleted(true);

        // insert successful task execution to DB
        addExecutionEntry(task);

        Log.d(TAG, String.valueOf(commOverhead));

        Log.d(TAG, "Broadcasting offloading result");
        Intent intent = new Intent(OFFLOADING_RESULT_SUB);
        intent.putExtra("result", executionResult);
        intent.putExtra("duration", executionTime);
        intent.putExtra("overhead", commOverhead);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void addExecutionEntry(TaskExecution task){
        framework.dbAdapter.addTaskExecution(task);
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
}
