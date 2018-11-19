package uk.ac.standrews.cs.mamoc.Sorting;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import uk.ac.standrews.cs.mamoc_client.MamocFramework;
import uk.ac.standrews.cs.mamoc_client.Execution.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class SortingActivity extends DemoBaseActivity {

    private final String task_name = QuickSort.class.getName();

    RadioGroup radioGroup;
    private TextView sortOutput;

    //variables
    private String fileSize;
    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        //views
        Button localButton = findViewById(R.id.buttonLocal);
        Button edgeButton = findViewById(R.id.buttonEdge);
        Button cloudButton = findViewById(R.id.buttonCloud);
        Button mamocButton = findViewById(R.id.buttonMamoc);

        sortOutput = findViewById(R.id.sortOutput);
        sortOutput.setMovementMethod(new ScrollingMovementMethod());

        radioGroup = findViewById(R.id.fileSizeRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.smallFileRadioButton) {
                fileSize = "small";
            } else if(checkedId == R.id.mediumFileRadioButton) {
                fileSize = "medium";
            } else {
                fileSize = "large";
            }
        });

        localButton.setOnClickListener(view -> sortText(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> sortText(ExecutionLocation.EDGE));
        cloudButton.setOnClickListener(view -> sortText(ExecutionLocation.PUBLIC_CLOUD));
        mamocButton.setOnClickListener(view -> sortText(ExecutionLocation.DYNAMIC));

        mamocFramework = MamocFramework.getInstance(this);
        mamocFramework.start();

        showBackArrow("Sorting Demo");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_sort;
    }

    private void sortText(ExecutionLocation location){

        if (fileSize == null){
            Toast.makeText(this, "Please select a file size", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (location){
            case LOCAL:
                sortLocal();
                break;
            case EDGE:
                runEdge();
                break;
            case PUBLIC_CLOUD:
                runCloud();
                break;
            case DYNAMIC:
                runDynamically();
                break;
        }
    }

    private void sortLocal() {
        mamocFramework.execute(ExecutionLocation.LOCAL, task_name, fileSize);
    }

    private void runEdge() {

        try{
            mamocFramework.execute(ExecutionLocation.EDGE, task_name, fileSize);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    private void runCloud() {
        try{
            mamocFramework.execute(ExecutionLocation.PUBLIC_CLOUD, task_name, fileSize);
        } catch (Exception e){
            Log.e("runCloud", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Cloud", Toast.LENGTH_SHORT).show();
        }
    }

    private void runDynamically() {
        try{
            mamocFramework.execute(ExecutionLocation.DYNAMIC, task_name, fileSize);
        } catch (Exception e){
            Log.e("Mamoc", e.getLocalizedMessage());
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        sortOutput.append("Execution returned " + result + "\n");
        sortOutput.append("Execution Duration: " + executationDuration + "\n");
        sortOutput.append("Communication Overhead: " + commOverhead + "\n");
        sortOutput.append("************************************************\n");
    }
}
