package uk.ac.standrews.cs.mamoc.SearchText;

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

public class SearchActivity extends DemoBaseActivity {

    private final String task_name = KMP.class.getName();

    RadioGroup radioGroup;
    private TextView keywordTextView, searchOutput;

    //variables
    private String keyword, fileSize;
    private MamocFramework mamocFramework;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        //views
        Button localButton = findViewById(R.id.buttonLocal);
        Button edgeButton = findViewById(R.id.buttonEdge);
        Button cloudButton = findViewById(R.id.buttonCloud);
        Button mamocButton = findViewById(R.id.buttonMamoc);

        keywordTextView = findViewById(R.id.searchEditText);
        searchOutput = findViewById(R.id.sortOutput);
        searchOutput.setMovementMethod(new ScrollingMovementMethod());

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
        cloudButton.setOnClickListener(View -> searchText(ExecutionLocation.PUBLIC_CLOUD));
        mamocButton.setOnClickListener(View -> searchText(ExecutionLocation.DYNAMIC));

        mamocFramework = MamocFramework.getInstance(this);
        mamocFramework.start();

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
     * @param location The location of execution of the method @ExecutionLocation
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
                runLocal(keyword);
                break;
            case EDGE:
                runEdge(keyword);
                break;
            case PUBLIC_CLOUD:
                runCloud(keyword);
                break;
            case DYNAMIC:
                runDynamically(keyword);
                break;
        }
    }

    private void runLocal(String keyword) {
        mamocFramework.execute(ExecutionLocation.LOCAL, task_name, fileSize, keyword);
    }

    private void runEdge(String keyword) {

        try{
            mamocFramework.execute(ExecutionLocation.EDGE, task_name, fileSize, keyword);
        } catch (Exception e){
            Log.e("runEdge", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Edge", Toast.LENGTH_SHORT).show();
        }
    }

    private void runCloud(String keyword) {

        try{
            mamocFramework.execute(ExecutionLocation.PUBLIC_CLOUD, task_name, fileSize, keyword);
        } catch (Exception e){
            Log.e("runCloud", e.getLocalizedMessage());
            Toast.makeText(this, "Could not execute on Cloud", Toast.LENGTH_SHORT).show();
        }
    }

    private void runDynamically(String keyword) {
        try{
            mamocFramework.execute(ExecutionLocation.DYNAMIC, task_name,  fileSize, keyword);
        } catch (Exception e){
            Log.e("Mamoc", e.getLocalizedMessage());
        }
    }

    @Override
    protected void addLog(String result, double executationDuration, double commOverhead) {
        searchOutput.append("Execution returned " + result + "\n");
        searchOutput.append("Execution Duration: " + executationDuration + "\n");
        searchOutput.append("Communication Overhead: " + commOverhead + "\n");
        searchOutput.append("************************************************\n");
    }
}
