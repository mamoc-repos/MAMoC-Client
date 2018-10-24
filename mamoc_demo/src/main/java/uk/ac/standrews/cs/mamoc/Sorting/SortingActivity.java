package uk.ac.standrews.cs.mamoc.Sorting;

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
import io.crossbar.autobahn.wamp.types.Publication;
import java8.util.concurrent.CompletableFuture;

import uk.ac.st_andrews.cs.mamoc_client.Communication.CommunicationController;
import uk.ac.st_andrews.cs.mamoc_client.Model.EdgeNode;
import uk.ac.st_andrews.cs.mamoc_client.profilers.ExecutionLocation;
import uk.ac.standrews.cs.mamoc.DemoBaseActivity;
import uk.ac.standrews.cs.mamoc.R;

import static uk.ac.st_andrews.cs.mamoc_client.Constants.WAMP_LOOKUP;

public class SortingActivity extends DemoBaseActivity {

    private final String RPC_NAME = "uk.ac.standrews.cs.mamoc.sorting.MergeSort";

    //views
    private Button localButton, edgeButton, cloudButton, mamocButton;
    RadioGroup radioGroup;
    private TextView sortOutput;

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

        sortOutput = findViewById(R.id.sortOutput);

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

        controller = CommunicationController.getInstance(this);

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
        String[] fileContentArray = fileContent.split(" ");

        showProgressDialog();

        // merge sort
        MergeSort.run(fileContentArray);

        long endTime = System.nanoTime();
        long MethodDuration = (endTime - startTime);

        addLog((double) MethodDuration *1.0e-9);

        hideDialog();
    }

    private void runEdge() {

        TreeSet<EdgeNode> edgeNodes = controller.getEdgeDevices();
        EdgeNode node = edgeNodes.first();
//        Log.d("edge:", String.valueOf(node.getCpuFreq()));

        if (node.session.isConnected()) {
            Log.d("Sending", "trying to call search procedure");

            // check if procedure is registered
            CompletableFuture<CallResult> registeredFuture = node.session.call(WAMP_LOOKUP, RPC_NAME);

            registeredFuture.thenAccept(registrationResult -> {
                if (registrationResult.results.get(0) == null) {
                    // Procedure not registered
                    Log.d("Sort", "not registered");
                    Toast.makeText(this, RPC_NAME + " not registered", Toast.LENGTH_SHORT).show();

                    try {
                        String sourceCode = controller.fetchSourceCode(RPC_NAME);
                        CompletableFuture<Publication> publishFuture = node.session.publish(
                                    "uk.standrews.cs.mamoc.android", sourceCode);
                        publishFuture.thenAccept(publishResult ->
                                    Log.d("publishResult", String.format("publish: %s",
                                            publishResult.publication))
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // Call the remote procedure.
                    Log.d("callResult", String.format("RPC ID: %s",
                            registrationResult.results.get(0)));

                    CompletableFuture<CallResult> callFuture = node.session.call(
                            RPC_NAME);
                    callFuture.thenAccept(callResult -> {
                        List<Object> results = (List) callResult.results.get(0);
                        Log.d("callResult", String.format("Took %s seconds",
                                results.get(0)));
                        addLog((double) results.get(0));
                    });
                }
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

    private void addLog(double duration) {
        sortOutput.append("Execution took: " + duration  + " seconds.\n");
    }
}
