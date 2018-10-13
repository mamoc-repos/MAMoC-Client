package uk.ac.standrews.cs.mamoc.SearchText;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class SearchActivity extends DemoBaseActivity {

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    RadioGroup radioGroup;
    private TextView keywordTextView, searchOutput;

    //variables
    private String keyword, fileSize;
    private CommunicationController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        KMP kmpSearch = new KMP();
        int result = kmpSearch.searchKMP(fileContent, keyword);

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog(result, (double) MethodDuration *1.0e-9);

        hideDialog();
    }

    private void searchEdge(String keyword) {

        TreeSet<EdgeNode> edgeNodes = controller.getEdgeDevices();
        EdgeNode node = edgeNodes.first();
        Log.d("edge:", String.valueOf(node.getCpuFreq()));

        if (node.session.isConnected()) {
            Log.d("Sending", "trying to call search procedure");

            // Call a remote procedure.
            CompletableFuture<CallResult> callFuture = node.session.call(
                    "uk.ac.standrews.cs.mamoc.search",
                    fileSize, keyword);
            callFuture.thenAccept(callResult -> {
                List<Object> results = (List) callResult.results.get(0);
                Log.d("callResult", String.format("Found: %s in %s seconds",
                        results.get(0), results.get(1)));
                addLog((int) results.get(0), (double) results.get(1));
            });
        } else {
            Toast.makeText(this, "Edge not connected!", Toast.LENGTH_SHORT).show();
        }
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

    private void addLog(int result, double duration) {
        if (result == 0) {
            searchOutput.append("no occurences found!\n");
        } else {
            searchOutput.append("Number of occurences: " + result + "\n");
            searchOutput.append("Execution took: " + duration  + " seconds.\n");
        }
    }
}
