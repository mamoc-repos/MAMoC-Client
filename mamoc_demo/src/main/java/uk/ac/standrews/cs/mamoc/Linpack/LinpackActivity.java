package uk.ac.standrews.cs.mamoc.Linpack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class LinpackActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.Linpack.Linpack";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    private TextView linpackOutput, nOutput;

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

        linpackOutput = findViewById(R.id.sortOutput);
        nOutput = findViewById(R.id.mandelBrotEditText);

        localButton.setOnClickListener(view -> runLinpack(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> runLinpack(ExecutionLocation.EDGE));

        showBackArrow("Linpack Demo");

        // runSimulation();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_mandelbrot;
    }

    private void runLinpack(ExecutionLocation location){

        N = Integer.parseInt(nOutput.getText().toString());

        if (N == 0){
            Toast.makeText(this, "Please enter N size", Toast.LENGTH_SHORT).show();
            return;
        }

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

        double result = new Linpack(N).run();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog(String.valueOf(result), MethodDuration * 1.0e-9, 0);
    }

    private void runEdge(int N) {

        try{
            controller.runRemote(LinpackActivity.this, ExecutionLocation.EDGE, RPC_NAME, "None", N);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        linpackOutput.append("Execution returned " + result + "\n");
        linpackOutput.append("Execution Duration: " + executationDuration + "\n");
        linpackOutput.append("Communication Overhead: " + commOverhead + "\n");
    }

    private void runSimulation(){
        int[] baseArray = {7, 25, 15, 4, 1, 10, 14, 24, 2, 22, 5, 23, 11, 18,
                20, 13, 6, 17, 3, 19, 16, 9, 12, 8, 21};

        double res =0.0;
        for(int i=0;i<baseArray.length;i++) {
            long startTime = System.nanoTime();

            res = new Linpack(baseArray[i] * 10).run();

            long endTime = System.nanoTime();
            long MethodDuration = (endTime - startTime);

            addLog(String.valueOf(res), MethodDuration * 1.0e-9, 0);
        }

    }
}
