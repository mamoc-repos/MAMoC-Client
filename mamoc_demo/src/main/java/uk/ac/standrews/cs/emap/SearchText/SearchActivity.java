package uk.ac.standrews.cs.emap.SearchText;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.standrews.cs.emap.R;

public class SearchActivity extends AppCompatActivity {

    //views
    private Button localButton, cloudletButton, cloudButton, mamocButton;
    private TextView keywordTextView, searchOutput;

    //variables
    private String keyword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        localButton = findViewById(R.id.buttonLocal);
        cloudletButton = findViewById(R.id.buttonCloudlet);
        cloudButton = findViewById(R.id.buttonCloud);
        mamocButton = findViewById(R.id.buttonMamoc);

        keywordTextView = findViewById(R.id.searchEditText);
        searchOutput = findViewById(R.id.searchOutput);

        localButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocal();
            }
        });

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
