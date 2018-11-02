package uk.ac.st_andrews.cs.mamoc_client.Communication;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Subscription;
import java8.util.concurrent.CompletableFuture;
import uk.ac.st_andrews.cs.mamoc_client.DexDecompiler;
import uk.ac.st_andrews.cs.mamoc_client.ExceptionHandler;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.MamocNode;
import uk.ac.st_andrews.cs.mamoc_client.Model.MobileNode;
import uk.ac.st_andrews.cs.mamoc_client.Annotation.Offloadable;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.OFFLOADING_PUB;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.OFFLOADING_RESULT_SUB;
import static uk.ac.st_andrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class CommunicationController {
    private final String TAG = "CommunicationController";

    private Context mContext;
    private int myPort;
    private ConnectionListener connListener;

    private static CommunicationController instance;

    private TreeSet<MobileNode> mobileDevices = new TreeSet<>();
    private TreeSet<EdgeNode> edgeDevices = new TreeSet<>();
    private TreeSet<CloudNode> cloudDevices = new TreeSet<>();

    private boolean isConnectionListenerRunning = false;

    private ArrayList<Class> offloadableClasses = new ArrayList<>();

    private final int STACK_SIZE = 20 * 1024 * 1024;
    private ExceptionHandler exceptionHandler;

    long startSendingTime, endSendingTime;

    private Subscription sub;

    private CommunicationController(Context context) {
        this.mContext = context;
        myPort = Utils.getPort(mContext);
        connListener = new ConnectionListener(mContext, myPort);

//        if (!checkAnnotatedIndexing()) {
            findOffloadableClasses();
//        }
    }

    private boolean checkAnnotatedIndexing() {
        String mamocDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mamoc";
        File mamocDir = new File(mamocDirPath);
        return mamocDir.exists();
    }

    private void findOffloadableClasses() {

        ThreadGroup group = new ThreadGroup("Dex to Java Group");

        Thread annotationIndexingThread = new Thread(group, (Runnable) () -> {

            Iterable<Class<?>> klasses = ClassIndex.getAnnotated(Offloadable.class);
            ArrayList<String> annotatedClasses = new ArrayList<>();

            for (Class<?> klass : klasses) {
                offloadableClasses.add(klass);
                annotatedClasses.add(klass.getName());
                Log.d("annotation", "new annotated class found: " + klass.getName());
            }

            decompileAnnotatedClassFiles(annotatedClasses);

        }, "Annotation Indexing Thread", STACK_SIZE);

        annotationIndexingThread.setPriority(Thread.MAX_PRIORITY);
        annotationIndexingThread.setUncaughtExceptionHandler(exceptionHandler);
        annotationIndexingThread.start();
    }

    private void decompileAnnotatedClassFiles(ArrayList<String> offloadableClasses) {
        DexDecompiler decompiler = new DexDecompiler(mContext, offloadableClasses);
        decompiler.runDecompiler();
    }

    private String fetchSourceCode(String className) {
        String ExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        String[] sourceClassPaths = className.split("\\.");

        StringBuilder sourceClass = new StringBuilder();

        for (String path : sourceClassPaths) {
            sourceClass.append(path).append("/");
        }

        sourceClass.deleteCharAt(sourceClass.length() - 1); // remove the extra / at the end
        sourceClass.append(".java");

        try {
            String fullPath = ExternalStoragePath + "/" + "mamoc" + "/" + sourceClass.toString();
            File sourceFile = new File(fullPath);
            Log.d("SourceFile", sourceFile.getAbsolutePath());
            return Utils.readFile(mContext, sourceFile.getAbsolutePath());

        } catch (Throwable x) {
            Log.e("error", "could not fetch the output directory");
        }

        return null;
    }

    public static CommunicationController getInstance(Context context) {
        if (instance == null) {
            synchronized (CommunicationController.class) {
                if (instance == null) {
                    instance = new CommunicationController(context);
                }
            }
        }
        return instance;
    }

    public void stopConnectionListener() {
        if (!isConnectionListenerRunning) {
            return;
        }
        if (connListener != null) {
            connListener.tearDown();
            connListener = null;
        }
        isConnectionListenerRunning = false;
    }

    public void startConnectionListener() {
        if (isConnectionListenerRunning) {
            return;
        }
        if (connListener == null) {
            connListener = new ConnectionListener(mContext, myPort);
        }
        if (!connListener.isAlive()) {
            connListener.interrupt();
            connListener.tearDown();
            connListener = null;
        }
        connListener = new ConnectionListener(mContext, myPort);
        connListener.start();
        isConnectionListenerRunning = true;
    }

    private void startConnectionListener(int port) {
        myPort = port;
        startConnectionListener();
    }

    public void restartConnectionListenerWith(int port) {
        stopConnectionListener();
        startConnectionListener(port);
    }

    public int getMyPort() {
        return myPort;
    }

    public boolean isConnectionListenerRunning() {
        return isConnectionListenerRunning;
    }

    public TreeSet<MobileNode> getMobileDevices() {
        return mobileDevices;
    }

    public void setMobileDevices(TreeSet<MobileNode> mobileDevices) {
        this.mobileDevices = mobileDevices;
    }

    public TreeSet<EdgeNode> getEdgeDevices() {
        return edgeDevices;
    }

    public void addEdgeDevice(EdgeNode edge) {
        this.edgeDevices.add(edge);
    }

    public void removeEdgeDevice(EdgeNode edge) {
        this.edgeDevices.remove(edge);
    }

    public TreeSet<CloudNode> getCloudDevices() {
        return cloudDevices;
    }

    public void addCloudDevices(CloudNode cloud) { this.cloudDevices.add(cloud); }

    public void removeCloudDevice(CloudNode cloud) {
        this.cloudDevices.remove(cloud);
    }

    public ArrayList<Class> getOffloadableClasses() {
        return offloadableClasses;
    }

    public void runLocal() {

    }

    public void runRemote(Context context, ExecutionLocation location, String rpc_name, String resource_name, Object... params) {

        switch (location) {
            case EDGE:
                runOnEdge(context, rpc_name, resource_name, params);
                break;
            case LOCAL:
                runLocal();
                break;
            case REMOTE_CLOUD:
                runOnCloud(context, rpc_name, resource_name, params);
                break;
        }
    }

    private void runOnEdge(Context context, String rpc_name, String resource_name, Object... params){
        TreeSet<EdgeNode> edgeNodes = getEdgeDevices();
        if (!edgeNodes.isEmpty()) {
            EdgeNode node = edgeNodes.first();

            runRemotely(context, node, rpc_name, resource_name, params);

        } else {
            Toast.makeText(context, "No edge node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOnCloud(Context context, String rpc_name, String resource_name, Object... params){
        TreeSet<CloudNode> cloudNodes = getCloudDevices();
        if (!cloudNodes.isEmpty()) {
            CloudNode node = cloudNodes.first();

            runRemotely(context, node, rpc_name, resource_name, params);

        } else {
            Toast.makeText(context, "No cloud node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runRemotely(Context context, EdgeNode node, String rpc_name, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  rpc_name + " procedure");

            startSendingTime = System.nanoTime();
            mContext = context;

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, rpc_name);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, rpc_name + " not registered");
                    Toast.makeText(context, rpc_name + " not registered", Toast.LENGTH_SHORT).show();

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

                        String sourceCode = fetchSourceCode(rpc_name);

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                rpc_name,
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
                    // Call the remote procedure.
                    Log.d(TAG, String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            rpc_name);
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

    private void runRemotely(Context context, CloudNode node, String rpc_name, String resource_name, Object[] params){

        if (node.session.isConnected()) {
            Log.d(TAG, "trying to call " +  rpc_name + " procedure");

            startSendingTime = System.nanoTime();
            mContext = context;

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, rpc_name);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d(TAG, rpc_name + " not registered");
                    Toast.makeText(context, rpc_name + " not registered", Toast.LENGTH_SHORT).show();

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

                        String sourceCode = fetchSourceCode(rpc_name);

                        // publish (offload) the source code
                        CompletableFuture<Publication> pubFuture = node.session.publish(
                                OFFLOADING_PUB,
                                "Android",
                                rpc_name,
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
                    // Call the remote procedure.
                    Log.d(TAG, String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            rpc_name);
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
        broadcastResults(results);
        sub.unsubscribe();
    }

    private void broadcastResults(List<Object> results){
        endSendingTime = System.nanoTime();
        double commOverhead = (double)(endSendingTime - startSendingTime) * 1.0e-9;
        commOverhead -=  (Double) results.get(1);

        Log.d(TAG, "Broadcasting offloading result");
        Intent intent = new Intent(OFFLOADING_RESULT_SUB);
        intent.putExtra("result", (String) results.get(0));
        intent.putExtra("duration", (Double) results.get(1));
        intent.putExtra("overhead", commOverhead);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
