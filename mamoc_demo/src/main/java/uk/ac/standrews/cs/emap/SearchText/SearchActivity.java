package uk.ac.standrews.cs.emap.SearchText;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.CallResult;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import io.crossbar.autobahn.wamp.types.SessionDetails;
import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.Model.CloudletNode;
import uk.ac.standrews.cs.emap.R;

public class SearchActivity extends AppCompatActivity {

    //views
    private Button localButton, cloudletButton, cloudButton, mamocButton;
    private TextView keywordTextView, searchOutput;

    //variables
    private String keyword;
    private CommunicationController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        controller = CommunicationController.getInstance(this);

        localButton = findViewById(R.id.buttonLocal);
        cloudletButton = findViewById(R.id.buttonCloudlet);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        keywordTextView = findViewById(R.id.searchEditText);
        searchOutput = findViewById(R.id.searchOutput);

        localButton.setOnClickListener(view -> searchLocal());
        cloudletButton.setOnClickListener(view -> searchCloudlet());

    }

    private void searchCloudlet() {

        keyword = keywordTextView.getText().toString();

        TreeSet<CloudletNode> cloudletNodes = controller.getCloudletDevices();
        CloudletNode node = cloudletNodes.first();
        Log.d("cloudlet:", String.valueOf(node.getCpuFreq()));

        // Call a remote procedure.
        CompletableFuture<CallResult> callFuture = node.session.call("com.arguments.add2", 2, 3);
//        callFuture.thenAccept(callResult -> System.out.println(String.format(
        callFuture.thenAccept(callResult ->
                Log.d("callResult", String.format("Call result: %s", callResult.results.get(0))));

    }
    

    private void searchLocal() {

        keyword = keywordTextView.getText().toString();

        if (keyword == null || keyword.isEmpty()){
            Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show();
            return;
        }

        long startTime = System.nanoTime();

        String fileContent = getContentFromTextFile("large.txt");

        KMP kmpSearch = new KMP();
       // int result = kmpSearch.KMPSearch(keyword, fileContent);
        int result = kmpSearch.searchKMP(fileContent, keyword).size();

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog(result, MethodDuration);
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

    private void addLog(int result, long duration) {
        if (result == 0) {
            searchOutput.append("no occurences found!\n");
        } else {
            searchOutput.append("Number of occurences: " + result + "\n");
            searchOutput.append("Local Execution took: " + (double)duration / 1000000000.0 + " seconds.\n");
        }
    }
}
