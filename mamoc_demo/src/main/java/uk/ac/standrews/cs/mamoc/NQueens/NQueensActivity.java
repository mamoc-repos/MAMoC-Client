package uk.ac.standrews.cs.mamoc.NQueens;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class NQueensActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.NQueens.Queens";

    private TextView nqueensOutput, nOutput;

    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        mamocFramework = MamocFramework.getInstance(this);
        mamocFramework.start();

        //views
        Button localButton = findViewById(R.id.buttonLocal);
        Button edgeButton = findViewById(R.id.buttonEdge);
        Button cloudButton = findViewById(R.id.buttonCloud);
        Button mamocButton = findViewById(R.id.buttonMamoc);

        nqueensOutput = findViewById(R.id.sortOutput);
        nOutput = findViewById(R.id.mandelBrotEditText);
        nOutput.setMovementMethod(new ScrollingMovementMethod());

        localButton.setOnClickListener(view -> runQueens(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> runQueens(ExecutionLocation.EDGE));
        cloudButton.setOnClickListener(view -> runQueens(ExecutionLocation.PUBLIC_CLOUD));
        mamocButton.setOnClickListener(view -> runQueens(ExecutionLocation.DYNAMIC));

        showBackArrow("NQueens Demo");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_nqueens;
    }

    private void runQueens(ExecutionLocation location) {

        //variables
        int n;
        if (nOutput.getText().toString().isEmpty()) {
        //    Toast.makeText(this, "Please enter N size", Toast.LENGTH_SHORT).show();
        //    return;
            n = 13;
        } else {
            n = Integer.parseInt(nOutput.getText().toString());
        }

        switch (location) {
            case LOCAL:
                runLocal(n);
                break;
            case EDGE:
                runEdge(n);
                break;
            case PUBLIC_CLOUD:
                runCloud(n);
                break;
            case DYNAMIC:
                runDynamically(n);
        }
    }

    private void runLocal(int N) {
        mamocFramework.execute(ExecutionLocation.LOCAL, RPC_NAME, "None", N);
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

    private void runDynamically(int N) {
        try{
            mamocFramework.execute(ExecutionLocation.DYNAMIC, RPC_NAME, "None", N);
        } catch (Exception e){
            Log.e("Mamoc", e.getLocalizedMessage());
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
