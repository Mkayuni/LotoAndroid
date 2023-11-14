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
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private List<String> skippedWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loto);

        // Initialize SensorManager and accelerometerSensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register SensorEventListener for tilt detection
        sensorManager.registerListener(new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                float zValue = event.values[2];
                // Check if the phone is tilted downwards (negative z-axis)
                if (zValue < -9.0f) {
                    onSkip();
                } else if (zValue > 9.0f) {
                    // Check if the phone is tilted upwards (positive z-axis)
                    onSkipUpwards();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not needed for this example
            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gameHandler = new Handler();
        wordPairTextView = findViewById(R.id.wordPairTextView);
        wordPairLayout = findViewById(R.id.wordPairLayout);

        inGameMode = false;
        setCategoryImageClickListeners();

        // Initialize the MediaPlayer for the tick-tock sound
        tickPlayer = MediaPlayer.create(this, R.raw.ticktock);
    }

    private void onSkip() {
        if (inGameMode) {
            // Show flash card saying "Correct"
            landscapeWordPairTextView.setText("CORRECT");

            // Delay for a short duration (e.g., 1000 milliseconds) to display "Correct"
            gameHandler.postDelayed(() -> {
                // Load and display the next word pair
                loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
            }, 500); // Delay
        }
    }

    private void onSkipUpwards() {
        if (inGameMode) {
            // Check if landscapeWordPairTextView is not null before using it
            if (landscapeWordPairTextView != null) {
                // Show flash card saying "SKIP" for upward tilt
                landscapeWordPairTextView.setText("SKIP");

                // Log to confirm that "SKIP" is set
                Log.d(TAG, "Word pair set to SKIP");

                // Delay for a short duration (e.g., 1000 milliseconds) to display "SKIP"
                gameHandler.postDelayed(() -> {
                    // Load and display the next word pair
                    String skippedWordPair = loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
                    // Store the skipped word pair
                    skippedWords.add(skippedWordPair);
                }, 500); // Delay
            } else {
                // Log an error if landscapeWordPairTextView is null
                Log.e(TAG, "landscapeWordPairTextView is null");
            }
        }
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
            // Show the prompt layout
            Log.d(TAG, "Switching to prompt layout");
            setContentView(R.layout.prompt_layout);

            // Listen for a click on the prompt layout
            findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // User acknowledged the prompt, switch to the landscape layout
                    Log.d(TAG, "Prompt acknowledged, switching to landscape layout");
                    switchToLandscapeLayout();
                }
            });
        } else {
            // Device is already in landscape mode or using a layout that works in portrait
            Log.d(TAG, "Switching directly to landscape layout");
            switchToLandscapeLayout();
        }
    }

    private void switchToLandscapeLayout() {
        Log.d(TAG, "Switching to landscape layout");
        setContentView(R.layout.landscape_text_display);
        landscapeWordPairTextView = findViewById(R.id.landscapeWordPairTextView);
        countdownTextView = findViewById(R.id.countdownTextView);

        if (landscapeWordPairTextView != null && countdownTextView != null) {
            loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
            countdownSeconds = 30;
            startGameCountdown();
        } else {
            // Handle the case where views are not properly initialized
            Log.e(TAG, "landscapeWordPairTextView or countdownTextView is null.");
        }
    }

    private String loadAndDisplayWordPairs(String selectedCategory, TextView targetTextView) {
        List<String> englishWords = new ArrayList<>();
        List<String> chichewaWords = new ArrayList<>();
        loadWordsForCategory(selectedCategory, englishWords, chichewaWords);

        Log.d(TAG, "Loaded " + englishWords.size() + " English Words and " + chichewaWords.size() + " Chichewa words for category " + selectedCategory);

        // Return the displayed word pair
        return displayRandomWordPair(englishWords, chichewaWords, targetTextView);
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
                    stopTickPlayer();
                    displayGameOverAndReturnToCategorySelection();
                } else {
                    gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
                    Log.d(TAG, "Countdown: " + countdownSeconds);

                    // Play tick-tock sound when the countdown is running
                    playTickPlayer();

                    // Check if there are 5 seconds or less left
                    if (countdownSeconds <= 5) {
                        speedUpTickPlayer();
                    }
                }
            }
        };

        gameHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL);
    }

    private void playTickPlayer() {
        if (tickPlayer != null && !tickPlayer.isPlaying()) {
            tickPlayer.start();
        }
    }

    private void stopTickPlayer() {
        if (tickPlayer != null) {
            tickPlayer.stop();
            tickPlayer.release();
            tickPlayer = null;
        }
    }

    private void speedUpTickPlayer() {
        playTickPlayer();
    }

    private void displayGameOverAndReturnToCategorySelection() {
        // Display "Game Over" using the string resource
        landscapeWordPairTextView.setText(R.string.game_over);

        // Display skipped words for 8 seconds
        if (!skippedWords.isEmpty()) {
            gameHandler.postDelayed(() -> {
                // Concatenate all skipped words into a single string
                StringBuilder skippedWordsText = new StringBuilder();
                for (String skippedWord : skippedWords) {
                    skippedWordsText.append(skippedWord).append("\n");
                }

                // Display all skipped words at once
                landscapeWordPairTextView.setText(skippedWordsText.toString());

                // Delay for a moment before resetting the game and returning to category selection
                gameHandler.postDelayed(() -> {
                    // Reset the game and start a new one
                    resetAndStartNewGame();

                    // Transition back to category selection
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }, 10000); // Delay for 2 seconds (2000 milliseconds) before returning to category selection
            }, 2000); // Delay for 8 seconds (8000 milliseconds) before displaying skipped words
        } else {
            // No skipped words, reset the game and start a new one
            gameHandler.postDelayed(() -> {
                resetAndStartNewGame();

                // Transition back to category selection
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }, 10000); // Delay for 2 seconds (2000 milliseconds) before returning to category selection
        }
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

    private String displayRandomWordPair(List<String> englishWords, List<String> chichewaWords, TextView targetTextView) {
        if (!inGameMode || englishWords.isEmpty() || chichewaWords.isEmpty()) {
            targetTextView.setText("");
            Log.d(TAG, "Word pairs are empty or not in game mode");
            return "";
        }

        Random random = new Random();
        int randomIndex = random.nextInt(englishWords.size());

        if (randomIndex < englishWords.size() && randomIndex < chichewaWords.size()) {
            String wordPair = englishWords.get(randomIndex) + " - " + chichewaWords.get(randomIndex);
            targetTextView.setText(wordPair);
            Log.d(TAG, "Displaying word pair: " + wordPair);
            // Return the displayed word pair
            return wordPair;
        } else {
            Log.e(TAG, "Random index is out of bounds");
            return "";
        }
    }
}



