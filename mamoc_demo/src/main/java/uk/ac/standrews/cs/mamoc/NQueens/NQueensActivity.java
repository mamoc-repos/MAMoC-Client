package uk.ac.standrews.cs.mamoc.NQueens;

import android.content.Intent;
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

import io.crossbar.autobahn.wamp.types.CallResult;
// supporting Android API < 24
import java8.util.concurrent.CompletableFuture;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class NQueensActivity extends DemoBaseActivity {

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

    private void runMandelbrot(ExecutionLocation location){

        if (nOutput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter N size", Toast.LENGTH_SHORT).show();
            return;
        }

        N = Integer.parseInt(nOutput.getText().toString());

        switch (location){
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

        addLog((double) MethodDuration *1.0e-9);

        hideDialog();
    }

    private void runEdge(int N) {

        TreeSet<EdgeNode> edgeNodes = controller.getEdgeDevices();
        EdgeNode node = edgeNodes.first();
        Log.d("edge:", String.valueOf(node.getCpuFreq()));

        if (node.session.isConnected()) {
            Log.d("Sending", "trying to call search procedure");

            // Call a remote procedure.
            CompletableFuture<CallResult> callFuture = node.session.call(
                    "uk.ac.standrews.cs.mamoc.nqueens", N);
            callFuture.thenAccept(callResult -> {
                List<Object> results = (List) callResult.results.get(0);
                Log.d("callResult", String.format("Took %s seconds",
                        results.get(0)));
                addLog((double) results.get(0));
            });
        } else {
            Toast.makeText(this, "Edge not connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLog(double duration) {
        nqueensOutput.append("Execution took: " + duration  + " seconds.\n");
    }
}
