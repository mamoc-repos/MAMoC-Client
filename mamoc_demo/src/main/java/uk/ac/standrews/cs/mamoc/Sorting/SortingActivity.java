package uk.ac.standrews.cs.mamoc.Sorting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.st_andrews.cs.mamoc_client.MamocFramework;
import uk.ac.st_andrews.cs.mamoc_client.Profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class SortingActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.sorting.QuickSort";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    RadioGroup radioGroup;
    private TextView sortOutput;

    //variables
    private String fileSize;
    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        localButton = findViewById(R.id.buttonLocal);
        edgeButton = findViewById(R.id.buttonEdge);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

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

        mamocFramework = MamocFramework.getInstance(this);

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
        }
    }

    private void sortLocal() {

        long startTime = System.nanoTime();

        String fileContent = getContentFromTextFile(fileSize + ".txt");

        showProgressDialog();

        // quick sort
        new QuickSort(fileContent).run();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog("nothing", (double) MethodDuration *1.0e-9, 0);

        hideDialog();
    }

    private void runEdge() {

        try{
            mamocFramework.execute(ExecutionLocation.EDGE, RPC_NAME, fileSize);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        sortOutput.append("Execution returned " + result + "\n");
        sortOutput.append("Execution Duration: " + executationDuration + "\n");
        sortOutput.append("Communication Overhead: " + commOverhead + "\n");
        sortOutput.append("************************************************\n");
    }

    private String getContentFromTextFile(String file) {

        String fileContent = null;
        try {
            fileContent = readFromAssets(this, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    private String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

}
