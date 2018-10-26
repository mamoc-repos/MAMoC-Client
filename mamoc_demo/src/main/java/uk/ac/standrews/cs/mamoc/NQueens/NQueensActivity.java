package uk.ac.standrews.cs.mamoc.NQueens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.TreeSet;

import io.crossbar.autobahn.wamp.types.CallResult;
// supporting Android API < 24
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Subscription;
import java8.util.concurrent.CompletableFuture;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class NQueensActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.nqueens.Queens";

    long startSendingTime, endSendingTime;

    private Subscription sub;

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    private TextView nqueensOutput, nOutput;

    //variables
    private int N;
    private CommunicationController controller;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        controller = CommunicationController.getInstance(this);

        localButton = findViewById(R.id.buttonLocal);
        edgeButton = findViewById(R.id.buttonEdge);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        nqueensOutput = findViewById(R.id.sortOutput);
        nOutput = findViewById(R.id.mandelBrotEditText);

        localButton.setOnClickListener(view -> runMandelbrot(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> runMandelbrot(ExecutionLocation.EDGE));

        showBackArrow("NQueens Demo");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_nqueens;
    }

    private void runMandelbrot(ExecutionLocation location) {

        if (nOutput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter N size", Toast.LENGTH_SHORT).show();
            return;
        }

        N = Integer.parseInt(nOutput.getText().toString());

        switch (location) {
            case LOCAL:
                runLocal(N);
                break;
            case EDGE:
                runEdge(N);
                break;
        }
    }

    private void runLocal(int N) {

        long startTime = System.nanoTime();
        startSendingTime = System.nanoTime();

        showProgressDialog();

        Queens.run(N);

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog("nothing", (double) MethodDuration * 1.0e-9);

        hideDialog();
    }

    private void runEdge(int N) {

        // TODO: move the logic of running remotely into controller
//        try{
//            controller.runRemote(this, ExecutionLocation.EDGE, RPC_NAME, N);
//        } catch (ExecutionException e){
//            Log.e("runEdge", e.getLocalizedMessage());
//            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
//        }

        TreeSet<EdgeNode> edgeNodes = controller.getEdgeDevices();
        if (!edgeNodes.isEmpty()) {
            EdgeNode node = edgeNodes.first();
            Log.d("edge:", String.valueOf(node.getCpuFreq()));

            if (node.session.isConnected()) {
                Log.d("Sending", "trying to call nqueens procedure");

                startSendingTime = System.nanoTime();

                // check if procedure is registered
                CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, RPC_NAME);

                registeredFuture.thenAccept(registrationResult -> {
                    if (registrationResult.results.get(0) == null) {
                        // Procedure not registered
                        Log.d("nqueens", "not registered");
                        Toast.makeText(this, RPC_NAME + " not registered", Toast.LENGTH_SHORT).show();

                        try {
                            // subscribe to the result of offloading
                            CompletableFuture<Subscription> subFuture = node.session.subscribe(
                                    "uk.ac.standrews.cs.mamoc.offloadingresult",
                                    this::onOffloadingResult);

                            subFuture.whenComplete((subscription, throwable) -> {
                                if (throwable == null) {

                                    sub = subscription;
                                    // We have successfully subscribed.
                                    Log.d("subscription", "Subscribed to topic " + subscription.topic);
                                } else {
                                    // Something went bad.
                                    throwable.printStackTrace();
                                }
                            });

                            String sourceCode = controller.fetchSourceCode(RPC_NAME);

                            // publish (offload) the source code
                            CompletableFuture<Publication> pubFuture = node.session.publish(
                                    "uk.ac.standrews.cs.mamoc.offloading",
                                    "Android", RPC_NAME, sourceCode, N);
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
                        Log.d("callResult", String.format("RPC ID: %s",
                                registrationResult.results.get(0)));

                        CompletableFuture<CallResult> callFuture = node.session.call(
                                RPC_NAME);
                        callFuture.thenAccept(callResult -> {
                            List<Object> results = (List) callResult.results.get(0);
//                            Log.d("callResult", String.format("Took %s seconds", results.get(0)));
                            addLog((String) results.get(0), (double) results.get(1));
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Edge is not connected!", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "No edge node exists", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOffloadingResult(List<Object> results) {
        addLog((String) results.get(0), (double) results.get(1));
        sub.unsubscribe();
    }

    private void addLog(String result, double duration) {
        nqueensOutput.append("Execution returned " + result + " and took: " + duration + " seconds.\n");

        endSendingTime = System.nanoTime();
        double commOverhead = (double)(endSendingTime - startSendingTime) * 1.0e-9;
        commOverhead -= duration;
        nqueensOutput.append("Communication overhead: " + commOverhead + " seconds.\n");
    }
}
