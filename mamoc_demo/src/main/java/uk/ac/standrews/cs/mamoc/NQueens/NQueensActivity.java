package uk.ac.standrews.cs.mamoc.NQueens;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.MamocFramework;
import uk.ac.st_andrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class NQueensActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.nqueens.Queens";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    private TextView nqueensOutput, nOutput;

    //variables
    private int N;
    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        mamocFramework = MamocFramework.getInstance(this);

        localButton = findViewById(R.id.buttonLocal);
        edgeButton = findViewById(R.id.buttonEdge);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        nqueensOutput = findViewById(R.id.sortOutput);
        nOutput = findViewById(R.id.mandelBrotEditText);
        nOutput.setMovementMethod(new ScrollingMovementMethod());

        localButton.setOnClickListener(view -> runQueens(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> runQueens(ExecutionLocation.EDGE));
        cloudButton.setOnClickListener(view -> runQueens(ExecutionLocation.PUBLIC_CLOUD));

        showBackArrow("NQueens Demo");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_nqueens;
    }

    private void runQueens(ExecutionLocation location) {

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
            case PUBLIC_CLOUD:
                runCloud(N);
                break;
        }
    }

    private void runLocal(int N) {

        long startTime = System.nanoTime();

        showProgressDialog();

        new Queens(N).run();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog("nothing", (double) MethodDuration * 1.0e-9, 0);

        hideDialog();
    }

    private void runEdge(int N) {
        try{
            mamocFramework.execute(ExecutionLocation.EDGE, RPC_NAME, "None", N);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    private void runCloud(int N) {
        try{
            mamocFramework.execute(ExecutionLocation.PUBLIC_CLOUD, RPC_NAME, "None", N);
        } catch (Exception e){
            Log.e("runCloud", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Cloud", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        nqueensOutput.append("Execution returned " + result + "\n");
        nqueensOutput.append("Execution Duration: " + executationDuration + "\n");
        nqueensOutput.append("Communication Overhead: " + commOverhead + "\n");
        nqueensOutput.append("************************************************\n");
    }
}
