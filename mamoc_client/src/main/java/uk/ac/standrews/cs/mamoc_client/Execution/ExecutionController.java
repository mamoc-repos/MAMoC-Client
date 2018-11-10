package uk.ac.standrews.cs.mamoc_client.Execution;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.TreeSet;

import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Subscription;
import java8.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Model.CloudNode;
import uk.ac.standrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.standrews.cs.mamoc_client.Profilers.ExecutionLocation;

import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_PUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.OFFLOADING_RESULT_SUB;
import static uk.ac.standrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class ExecutionController {

    private final String TAG = "ExecutionController";

    private Context mContext;

    private static ExecutionController instance;

    private Subscription sub;
    long startSendingTime, endSendingTime;

    private ExecutionController(Context context) {
        this.mContext = context;
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

    public void runLocal() {

    }

    public void runRemote(Context context, ExecutionLocation location, String rpc_name, String resource_name, Object... params) {

        switch (location) {
//            case LOCAL:
//                runLocal();
//                break;

            case D2D:
                runNearby();
                break;

            case EDGE:
                runOnEdge(context, rpc_name, resource_name, params);
                break;

            case PUBLIC_CLOUD:
                runOnCloud(context, rpc_name, resource_name, params);
                break;

            case DYNAMIC:
                runDynamically(context, rpc_name, resource_name, params);
                break;
        }
    }

    private void runDynamically(Context context, String rpc_name, String resource_name, Object[] params) {
        MamocFramework.getInstance(context).decisionEngine.makeDecision(rpc_name, false);
    }

    private void runNearby() {

    }

    private void runOnEdge(Context context, String rpc_name, String resource_name, Object... params){
        TreeSet<EdgeNode> edgeNodes = MamocFramework.getInstance(context).commController.getEdgeDevices();
        if (!edgeNodes.isEmpty()) {
            EdgeNode node = edgeNodes.first();
            runRemotely(context, node, rpc_name, resource_name, params);
        } else {
            Toast.makeText(context, "No edge node exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void runOnCloud(Context context, String rpc_name, String resource_name, Object... params){
        TreeSet<CloudNode> cloudNodes = MamocFramework.getInstance(context).commController.getCloudDevices();
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

                        String sourceCode =  MamocFramework.getInstance(context).fetchSourceCode(rpc_name);

                        startSendingTime = System.nanoTime();

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

                        String sourceCode =  MamocFramework.getInstance(context).fetchSourceCode(rpc_name);

                        startSendingTime = System.nanoTime();

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
        endSendingTime = System.nanoTime();
        broadcastResults(results);
        sub.unsubscribe();
    }

    private void broadcastResults(List<Object> results){
        double commOverhead = (double)(endSendingTime - startSendingTime) * 1.0e-9;
        Log.d(TAG, String.valueOf(commOverhead));

        commOverhead -=  (Double) results.get(1);

        Log.d(TAG, String.valueOf(commOverhead));

        Log.d(TAG, "Broadcasting offloading result");
        Intent intent = new Intent(OFFLOADING_RESULT_SUB);
        intent.putExtra("result", (String) results.get(0));
        intent.putExtra("duration", (Double) results.get(1));
        intent.putExtra("overhead", commOverhead);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
