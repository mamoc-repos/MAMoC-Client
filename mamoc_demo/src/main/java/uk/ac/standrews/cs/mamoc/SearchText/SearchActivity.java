package uk.ac.standrews.cs.mamoc.SearchText;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

public class SearchActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.SearchText.KMP";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    RadioGroup radioGroup;
    private TextView keywordTextView, searchOutput;

    //variables
    private String keyword, fileSize;
    private CommunicationController controller;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        localButton = findViewById(R.id.buttonLocal);
        edgeButton = findViewById(R.id.buttonEdge);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        keywordTextView = findViewById(R.id.searchEditText);
        searchOutput = findViewById(R.id.sortOutput);

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

        localButton.setOnClickListener(view -> searchText(ExecutionLocation.LOCAL));
        edgeButton.setOnClickListener(view -> searchText(ExecutionLocation.EDGE));

        controller = CommunicationController.getInstance(this);

        showBackArrow("Searching Demo");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_search;
    }

    /**
     * * This method is called when
     *  * <p>
     *  *
     *  *
     * @param location
     */
    private void searchText(ExecutionLocation location){

        if (fileSize == null){
            Toast.makeText(this, "Please select a file size", Toast.LENGTH_SHORT).show();
            return;
        }

        keyword = keywordTextView.getText().toString();

        if (keyword == null || keyword.isEmpty()){
            Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (location){
            case LOCAL:
                searchLocal(keyword);
                break;
            case EDGE:
                searchEdge(keyword);
                break;
        }
    }

    private void searchLocal(String keyword) {

        long startTime = System.nanoTime();

        String fileContent = getContentFromTextFile(fileSize + ".txt");

        showProgressDialog();

        int result = new KMP(fileContent, keyword).run();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog(String.valueOf(result), (double) MethodDuration *1.0e-9, 0);

        hideDialog();
    }

    private void searchEdge(String keyword) {

        String fileContent = getContentFromTextFile(fileSize + ".txt");

        try{
            controller.runRemote(this, ExecutionLocation.EDGE, RPC_NAME, fileSize, keyword);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        searchOutput.append("Execution returned " + result + "\n");
        searchOutput.append("Execution Duration: " + executationDuration + "\n");
        searchOutput.append("Communication Overhead: " + commOverhead + "\n");
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
