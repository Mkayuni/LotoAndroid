package com.example.lotoandroid;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LOTO extends AppCompatActivity {
    private static final String TAG = "LOTO";

    private TextView wordPairTextView;
    private List<String> englishWords;
    private List<String> chichewaWords;
    private int currentWordIndex = 0;
    private CountDownTimer timer;
    private static final long TIMER_DURATION = 30000;
    private TextView timerTextView;
    private TextView instructionMessageTextView;
    private Handler gameHandler;
    private Runnable countdownRunnable;
    private long remainingTime;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check the current orientation and set it to landscape if needed
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.landscape_text_display);

        wordPairTextView = findViewById(R.id.landscapeWordPairTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // Display "Game Over" initially in landscapeWordPairTextView
        wordPairTextView.setText(R.string.game_over);

        // Get the remaining time from the intent
        remainingTime = getIntent().getLongExtra("remainingTime", TIMER_DURATION);
        startTimer(remainingTime);

        // Hide the instruction message
        instructionMessageTextView.setVisibility(View.INVISIBLE);

        gameHandler = new Handler();

        // Load and display word pairs
        loadAndDisplayWordPairs(selectedCategory, wordPairTextView);
        startGameCountdown();
    }

    private void startTimer(long durationMillis) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(durationMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                updateTimerText(millisUntilFinished);
            }

            public void onFinish() {
                displayGameOverAndReturnToCategorySelection();
            }
        }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        int seconds = (int) (millisUntilFinished / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        String timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timerText);
    }

    public void loadWordsForCategory(String category) {
        englishWords = new ArrayList<>();
        chichewaWords = new ArrayList<>();

        try {
            AssetManager assetManager = getAssets();
            String fileName = category + ".txt";
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    englishWords.add(parts[0]);
                    chichewaWords.add(parts[1]);
                }
            }

            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shuffleWordPairs() {
        List<String> combinedPairs = new ArrayList<>();
        for (int i = 0; i < englishWords.size(); i++) {
            combinedPairs.add(englishWords.get(i) + " - " + chichewaWords.get(i));
        }

        Collections.shuffle(combinedPairs);

        englishWords.clear();
        chichewaWords.clear();

        for (String combinedPair : combinedPairs) {
            String[] parts = combinedPair.split(" - ");
            englishWords.add(parts[0]);
            chichewaWords.add(parts[1]);
        }
    }

    public void displayWordPair() {
        if (currentWordIndex < englishWords.size()) {
            String wordPair = englishWords.get(currentWordIndex) + " - " + chichewaWords.get(currentWordIndex);
            wordPairTextView.setText(wordPair);
        } else {
            wordPairTextView.setText(R.string.game_over);
        }
    }

    private void startGameCountdown() {
        countdownRunnable = () -> {
            remainingTime -= 1000;

            if (remainingTime <= 0) {
                displayGameOverAndReturnToCategorySelection();
            } else {
                startGameCountdown();
            }
        };
        gameHandler.postDelayed(countdownRunnable, 1000);
    }

    private void loadAndDisplayWordPairs(String category, TextView targetTextView) {
        loadWordsForCategory(category);

        // Check if there are words loaded
        if (englishWords.size() > 0 && chichewaWords.size() > 0) {
            shuffleWordPairs();  // Shuffle the word pairs
            displayWordPair();   // Display the first word pair
        } else {
            // Handle the case where no words are available for the selected category
            // You can display an error message or take appropriate action here.
        }
    }


    private void displayGameOverAndReturnToCategorySelection() {
        wordPairTextView.setText(R.string.game_over);

        gameHandler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}