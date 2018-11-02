package uk.ac.standrews.cs.mamoc.Benchmark;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class BenchmarkActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.Benchmark.Benchmark";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    private TextView benchmarkOutput;

    //variables
    private CommunicationController controller;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        controller = CommunicationController.getInstance(this);

        localButton = findViewById(R.id.buttonLocal);
        edgeButton = findViewById(R.id.buttonEdge);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        benchmarkOutput = findViewById(R.id.sortOutput);
        benchmarkOutput.setMovementMethod(new ScrollingMovementMethod());

        localButton.setOnClickListener(view -> runBenchmark(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> runBenchmark(ExecutionLocation.EDGE));

        showBackArrow("Benchmarking MAMoC");

        // runSimulation();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_benchmark;
    }

    private void runBenchmark(ExecutionLocation location){

        switch (location){
            case LOCAL:
                runLocal();
                break;
            case EDGE:
                runEdge();
                break;
        }
    }

    private void runLocal() {
        long startTime = System.nanoTime();

        double result = new Benchmark().run();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog(String.valueOf(result), MethodDuration * 1.0e-9, 0);
    }

    private void runEdge() {

        try{
            controller.runRemote(BenchmarkActivity.this, ExecutionLocation.EDGE, RPC_NAME, "None");
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        benchmarkOutput.append("Execution returned " + result + "\n");
        benchmarkOutput.append("Execution Duration: " + executationDuration + "\n");
        benchmarkOutput.append("Communication Overhead: " + commOverhead + "\n");
        benchmarkOutput.append("************************************************\n");
    }

//    private void runSimulation(){
//        int[] baseArray = {7, 25, 15, 4, 1, 10, 14, 24, 2, 22, 5, 23, 11, 18,
//                20, 13, 6, 17, 3, 19, 16, 9, 12, 8, 21};
//
//        double res =0.0;
//        for(int i=0;i<baseArray.length;i++) {
//            long startTime = System.nanoTime();
//
//            res = new Benchmark(baseArray[i] * 10).run();
//
//            long endTime = System.nanoTime();
//            long MethodDuration = (endTime - startTime);
//
//            addLog(String.valueOf(res), MethodDuration * 1.0e-9, 0);
//        }
//
//    }
}
