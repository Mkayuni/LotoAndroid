package com.example.lotoandroid;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Button;
import android.view.View;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView wordPairTextView;
    private String selectedCategory = "";
    private boolean inGameMode = false;
    private TextView landscapeWordPairTextView;
    private Handler gameHandler;
    private final int COUNTDOWN_INTERVAL = 1000;
    private int countdownSeconds = 30;
    private LinearLayout wordPairLayout;
    private TextView countdownTextView;
    private Runnable countdownRunnable; // Variable to track countdown
    MediaPlayer tickPlayer;
    private Button skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loto);


        gameHandler = new Handler();
        wordPairTextView = findViewById(R.id.wordPairTextView);
        wordPairLayout = findViewById(R.id.wordPairLayout);

        inGameMode = false;
        setCategoryImageClickListeners();

        // Initialize the MediaPlayer for the tick-tock sound
        tickPlayer = MediaPlayer.create(this, R.raw.ticktock);
        ;
    }

    private void setCategoryImageClickListeners() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ImageView commonPhrasesImage = findViewById(R.id.categoryImageCommonPhrases);
            ImageView commonWordsImage = findViewById(R.id.categoryImageCommonWords);
            ImageView animalsImage = findViewById(R.id.categoryImageAnimals);

            if (commonPhrasesImage != null) {
                commonPhrasesImage.setOnClickListener(view -> onCategoryImageClick("Common Phrases"));
            }

            if (commonWordsImage != null) {
                commonWordsImage.setOnClickListener(view -> onCategoryImageClick("Common Words"));
            }

            if (animalsImage != null) {
                animalsImage.setOnClickListener(view -> onCategoryImageClick("Animals"));
            }
        }
    }

    private void onCategoryImageClick(String category) {
        if (inGameMode) {
            resetAndStartNewGame();
        }

        selectedCategory = category;
        inGameMode = true;

        int orientation = getResources().getConfiguration().orientation;
        Log.d(TAG, "Orientation: " + orientation);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "Switching to landscape layout");
            setContentView(R.layout.landscape_text_display);
            landscapeWordPairTextView = findViewById(R.id.landscapeWordPairTextView);
            countdownTextView = findViewById(R.id.countdownTextView);

            // Find the skipButton
            skipButton = findViewById(R.id.skipButton);
            // Set an OnClickListener for the skipButton
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSkipButtonClick();
                }
            });

            if (landscapeWordPairTextView != null && countdownTextView != null) {
                loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
                countdownSeconds = 30;
                startGameCountdown();
            } else {
                // Handle the case where views are not properly initialized
                Log.e(TAG, "landscapeWordPairTextView or countdownTextView is null.");
            }
        }
    }

    private void onSkipButtonClick() {
        if (inGameMode) {
            loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
        }
    }
    private void loadAndDisplayWordPairs(String selectedCategory, TextView targetTextView) {
        List<String> englishWords = new ArrayList<>();
        List<String> chichewaWords = new ArrayList<>();
        loadWordsForCategory(selectedCategory, englishWords, chichewaWords);

        Log.d(TAG, "Loaded " + englishWords.size() + " English Words and " + chichewaWords.size() + " Chichewa words for category " + selectedCategory);

        displayRandomWordPair(englishWords, chichewaWords, targetTextView);
    }

    private void resetAndStartNewGame() {
        inGameMode = false;
        countdownSeconds = 30;
        wordPairTextView.setText("");

        // Reset the screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setCategoryImageClickListeners();
    }

    private void startGameCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                countdownSeconds--;

                countdownTextView.setText(String.valueOf(countdownSeconds));

                if (countdownSeconds <= 0) {
                    if (tickPlayer != null) {
                        tickPlayer.stop();
                        tickPlayer.release();
                    }
                    displayGameOverAndReturnToCategorySelection();
                } else {
                    gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
                    Log.d(TAG, "Countdown: " + countdownSeconds);

                    // Play tick-tock sound when the countdown is running
                    if (tickPlayer != null && !tickPlayer.isPlaying()) {
                        tickPlayer.start();
                    }

                    // Check if there are 5 seconds or less left
                    if (countdownSeconds <= 5) {
                        speedUpTickPlayer();
                    }
                }
            }
        };

        gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
    }

    private void speedUpTickPlayer() {
        if (tickPlayer != null && !tickPlayer.isPlaying()) {
            tickPlayer.start();
        }
    }
    private void displayGameOverAndReturnToCategorySelection() {
        // Display "Game Over" using the string resource
        landscapeWordPairTextView.setText(R.string.game_over);

        // Delay for a moment before returning to the category selection
        gameHandler.postDelayed(() -> {
            // Reset the game and start a new one
            resetAndStartNewGame();
            // Transition back to category selection
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }, 2000); // Delay for 2 seconds (2000 milliseconds) before returning to category selection
    }

    private void loadWordsForCategory(String category, List<String> englishWords, List<String> chichewaWords) {
        try {
            String fileName = category + ".txt";
            InputStream inputStream = getAssets().open(fileName);
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
            Log.e(TAG, "Error loading words for category " + category + ": " + e.getMessage());
        }
    }

    private void displayRandomWordPair(List<String> englishWords, List<String> chichewaWords, TextView targetTextView) {
        if (!inGameMode || englishWords.isEmpty() || chichewaWords.isEmpty()) {
            targetTextView.setText("");
            Log.d(TAG, "Word pairs are empty or not in game mode");
            return;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(englishWords.size());

        if (randomIndex < englishWords.size() && randomIndex < chichewaWords.size()) {
            String wordPair = englishWords.get(randomIndex) + " - " + chichewaWords.get(randomIndex);
            targetTextView.setText(wordPair);
            Log.d(TAG, "Displaying word pair: " + wordPair);
        } else {
            Log.e(TAG, "Random index is out of bounds");
        }
    }
}


