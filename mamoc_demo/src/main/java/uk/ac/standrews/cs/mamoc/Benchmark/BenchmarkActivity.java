package uk.ac.standrews.cs.mamoc.Benchmark;

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

public class BenchmarkActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.Benchmark.Benchmark";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    private TextView benchmarkOutput;

    //variables
    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        mamocFramework = MamocFramework.getInstance(this);

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
            mamocFramework.execute(ExecutionLocation.EDGE, RPC_NAME, "None");
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

    private void runSimulation(){

        for(int i=0;i<3;i++) {
            long startTime = System.nanoTime();

            double res = new Benchmark().run();

            long endTime = System.nanoTime();
            long MethodDuration = (endTime - startTime);

            addLog(String.valueOf(res), MethodDuration * 1.0e-9, 0);
        }
    }
}
