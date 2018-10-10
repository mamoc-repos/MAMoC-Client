package uk.ac.standrews.cs.emap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import uk.ac.st_andrews.cs.mamoc_client.DiscoveryActivity;
import uk.ac.standrews.cs.emap.SearchText.SearchActivity;

public class MainActivity extends AppCompatActivity {

    private Button searchTextDemo, discoveryBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discoveryBtn = findViewById(R.id.buttonDiscovery);
        discoveryBtn.setOnClickListener(view -> openDiscoveryActivity());

        searchTextDemo = findViewById(R.id.buttonSearchText);
        searchTextDemo.setOnClickListener(view -> openSearchTextDemo());
    }

    private void openDiscoveryActivity() {
        Intent discoveryIntent = new Intent(this, DiscoveryActivity.class);
        startActivity(discoveryIntent);
    }

    private void openSearchTextDemo() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivity(searchIntent);
    }
}
