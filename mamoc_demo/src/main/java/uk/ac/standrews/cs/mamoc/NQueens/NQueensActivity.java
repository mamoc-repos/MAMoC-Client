package uk.ac.standrews.cs.mamoc.NQueens;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Future;

import dalvik.system.DexClassLoader;
import io.crossbar.autobahn.wamp.types.CallResult;
// supporting Android API < 24
import io.crossbar.autobahn.wamp.types.Publication;
import java8.util.concurrent.CompletableFuture;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.DexDecompiler;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.Utils.Utils;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class NQueensActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.nqueens";

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

        showProgressDialog();

        Queens.run(N);

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog((double) MethodDuration * 1.0e-9);

        hideDialog();
    }

    private void runEdge(int N) {

        TreeSet<EdgeNode> edgeNodes = controller.getEdgeDevices();
        EdgeNode node = edgeNodes.first();
        Log.d("edge:", String.valueOf(node.getCpuFreq()));

        if (node.session.isConnected()) {
            Log.d("Sending", "trying to call search procedure");

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, RPC_NAME);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d("NQUEENS", "not registered");
                    Toast.makeText(this, RPC_NAME + " not registered", Toast.LENGTH_SHORT).show();

                    try {
                        DexDecompiler decompiler = new DexDecompiler(this, "uk.ac.standrews.cs.mamoc.NQueens.Queens");
                        decompiler.runDecompiler();

                        CompletableFuture<String> result = decompiler.fetchSourceCode();
                        result.thenAccept(codeResult -> {
                            CompletableFuture<Publication> publishFuture = node.session.publish(
                                    "uk.standrews.cs.mamoc.android", codeResult);
                            publishFuture.thenAccept(publishResult ->
                                    Log.d("publishResult", String.format("publish: %s",
                                            publishResult.publication)));
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // Call a remote procedure.
                    Log.d("callResult", String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            RPC_NAME, N);
                    callFuture.thenAccept(callResult -> {
                        List<Object> results = (List) callResult.results.get(0);
                        Log.d("callResult", String.format("Took %s seconds",
                                results.get(0)));
                        addLog((double) results.get(0));
                    });
                }
            });
        } else {
            Toast.makeText(this, "Edge not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLog(double duration) {
        nqueensOutput.append("Execution took: " + duration + " seconds.\n");
    }
}
