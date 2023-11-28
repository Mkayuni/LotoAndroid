package com.example.lotoandroid;

import android.os.Bundle;
import android.util.Log; // Import Log class
import android.widget.TextView;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class LandscapeActivity extends MainActivity {

    // Declare variables used in LandscapeActivity
    private TextView landscapeWordPairTextView;
    private TextView countdownTextView;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Example: Set result data before finishing the activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultData", "Some result data");
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landscape_text_display);

        // Retrieve the selected category from the intent
        Intent intent = getIntent();
        String selectedCategory = intent.getStringExtra("selectedCategory");

        // Use the selected category and set up your landscape layout as needed

        // Set the screen orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Example: You can use the selected category here if needed
    }


}