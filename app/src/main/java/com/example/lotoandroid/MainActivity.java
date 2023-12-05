package com.example.lotoandroid;

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.View;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;


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
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private List<String> skippedWords = new ArrayList<>();
    private boolean hasSkipped = false;
    private boolean hasDisplayedCorrectMessage = false;
    private boolean isClickListenerEnabled = true;
    private int correctWordCount = 0;
    private static final int ANIMATION_FADE_BLINK = R.anim.fade_blink;


    // Declare the SensorEventListener as a member variable
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float zValue = event.values[2];

            // Log the zValue for debugging purposes
            Log.d(TAG, "Z-axis value: " + zValue);

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
            // Log the accuracy change for debugging purposes
            Log.d(TAG, "Accuracy changed for sensor: " + sensor.getName() + ", Accuracy: " + accuracy);
            // Not needed for this example
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the sensorEventListener to avoid memory leaks
        if (sensorManager != null && sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loto);


        // Start the blinking animation for each category
        startBlinkAnimationForCategory("Common Phrases");
        startBlinkAnimationForCategory("Common Words");
        startBlinkAnimationForCategory("Animals");

        // Initialize SensorManager and accelerometerSensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        landscapeWordPairTextView = findViewById(R.id.landscapeWordPairTextView);


        if (sensorManager != null && accelerometerSensor != null) {
            // Register SensorEventListener for tilt detection
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Log an error if either sensorManager or accelerometerSensor is null
            Log.e(TAG, "SensorManager or accelerometerSensor is null");
        }


        gameHandler = new Handler();
        wordPairTextView = findViewById(R.id.wordPairTextView);
        wordPairLayout = findViewById(R.id.wordPairLayout);

        inGameMode = false;
        setCategoryImageClickListeners();

        // Initialize the MediaPlayer for the tick-tock sound
        tickPlayer = MediaPlayer.create(this, R.raw.ticktock);
    }

    private void onSkip() {
        if (inGameMode && !hasDisplayedCorrectMessage && isClickListenerEnabled) {
            // Increment the correct word count
            correctWordCount++;

            // Show flash card saying "Correct" with the style applied
            String correctMessage = getString(R.string.correct_message);

            // Create a SpannableString to apply style only to the "Correct" part
            SpannableString spannableString = new SpannableString(correctMessage);
            spannableString.setSpan(new TextAppearanceSpan(this, R.style.CorrectMessage), 0, correctMessage.length(), 0);

            // Log before setting text to check if landscapeWordPairTextView is null
            Log.d(TAG, "Before setting text. landscapeWordPairTextView is null: " + (landscapeWordPairTextView == null));

            // Check if landscapeWordPairTextView is not null before setting text
            if (landscapeWordPairTextView != null) {
                // Set the SpannableString to the TextView
                landscapeWordPairTextView.setText(spannableString);

                // Log that "Correct" is being displayed
                Log.d(TAG, "Word pair set to " + correctMessage);
            } else {
                // Log an error if landscapeWordPairTextView is null
                Log.e(TAG, "landscapeWordPairTextView is null. Unable to set text.");
                return; // Exit the method to avoid further execution
            }

            // Set hasDisplayedCorrectMessage to true to prevent displaying the correct message again until the next round
            hasDisplayedCorrectMessage = true;

            // Log to check the value of hasDisplayedCorrectMessage
            Log.d(TAG, "hasDisplayedCorrectMessage is now: " + hasDisplayedCorrectMessage);

            // Delay for a short duration
            gameHandler.postDelayed(() -> {
                // Load and display the next word pair
                String displayedWordPair = loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);

                // Log the displayed word pair
                Log.d(TAG, "Displayed word pair: " + displayedWordPair);

                // Reset hasDisplayedCorrectMessage to allow displaying the correct message in the next round
                hasDisplayedCorrectMessage = false;

                // Log to check the value of hasDisplayedCorrectMessage after reset
                Log.d(TAG, "hasDisplayedCorrectMessage reset to: " + hasDisplayedCorrectMessage);
            }, 500); // Delay
        }
    }

    private void onSkipUpwards() {
        if (inGameMode && !hasSkipped && isClickListenerEnabled) {
            // Check if landscapeWordPairTextView is not null before using it
            if (landscapeWordPairTextView != null) {
                // Show flash card saying "SKIP" for upward tilt
                String skipMessage = getString(R.string.skip_message);
                SpannableString spannableString = new SpannableString(skipMessage);

                // Apply style only to the "SKIP" part
                spannableString.setSpan(new TextAppearanceSpan(this, R.style.SkipMessage), 0, skipMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                landscapeWordPairTextView.setText(spannableString);

                // Log to confirm that "SKIP" is set
                Log.d(TAG, "Word pair set to " + skipMessage);

                // Set hasSkipped to true to prevent further skips until the next round
                hasSkipped = true;

                // Log to check the value of hasSkipped
                Log.d(TAG, "hasSkipped is now: " + hasSkipped);

                // Delay for a short duration (e.g., 1000 milliseconds) to display "SKIP"
                gameHandler.postDelayed(() -> {
                    // Load and display the next word pair
                    String skippedWordPair = loadAndDisplayWordPairs(selectedCategory, landscapeWordPairTextView);
                    // Store the skipped word pair
                    skippedWords.add(skippedWordPair);

                    // Reset hasSkipped to allow the next skip
                    hasSkipped = false;

                    // Log to check the value of hasSkipped after reset
                    Log.d(TAG, "hasSkipped reset to: " + hasSkipped);
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
                commonPhrasesImage.setOnClickListener(view -> {
                    if (isClickListenerEnabled) {
                        onCategoryImageClick("Common Phrases");
                    }
                });
            }

            if (commonWordsImage != null) {
                commonWordsImage.setOnClickListener(view -> {
                    if (isClickListenerEnabled) {
                        onCategoryImageClick("Common Words");
                    }
                });
            }

            if (animalsImage != null) {
                animalsImage.setOnClickListener(view -> {
                    if (isClickListenerEnabled) {
                        onCategoryImageClick("Animals");
                    }
                });
            }
        }
    }

    private void onCategoryImageClick(String category) {
        if (inGameMode && isClickListenerEnabled) {
            resetAndStartNewGame();
        }

        selectedCategory = category;
        inGameMode = true;

        int orientation = getResources().getConfiguration().orientation;
        Log.d(TAG, "Orientation: " + orientation);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Show the prompt layout with fade-in animation
            Log.d(TAG, "Switching to prompt layout with fade-in animation");
            setContentView(R.layout.prompt_layout);

            // Get the root layout for the animation
            View rootView = findViewById(android.R.id.content);

            // Load the fade-in animation
            Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

            // Apply the animation to the root layout
            rootView.startAnimation(fadeInAnimation);

            // Apply rotation and fade animations to the clicked images
            applyImageAnimation(R.id.categoryImageCommonPhrases, R.anim.blink);
            applyImageAnimation(R.id.categoryImageCommonWords, R.anim.blink);
            applyImageAnimation(R.id.categoryImageAnimals, R.anim.blink);

            // Start the blinking animation for the selected category
            startBlinkAnimationForCategory(category);

            // Listen for a click on the prompt layout
            rootView.setOnClickListener(new View.OnClickListener() {
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

    // Method to start the blinking animation for a specific category
    private void startBlinkAnimationForCategory(String category) {
        int animationId = R.anim.fade_blink;

        switch (category) {
            case "Common Phrases":
                applyImageAnimation(R.id.categoryImageCommonPhrases, animationId);
                break;
            case "Common Words":
                applyImageAnimation(R.id.categoryImageCommonWords, animationId);
                break;
            case "Animals":
                applyImageAnimation(R.id.categoryImageAnimals, animationId);
                break;
            // Add more cases for other categories if needed
        }
    }

    private void applyImageAnimation(int imageId, int animationId) {
        ImageView imageView = findViewById(imageId);
        if (imageView != null) {
            Log.d(TAG, "Applying animation to ImageView: " + imageId);

            Animation imageAnimation = AnimationUtils.loadAnimation(this, animationId);
            imageAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "Animation started");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "Animation ended");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    Log.d(TAG, "Animation repeated");
                }
            });

            imageView.startAnimation(imageAnimation);
        } else {
            Log.e(TAG, "ImageView is null. Cannot apply animation.");
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
                    if (countdownSeconds == 3) {
                        // Switch to a different sound file at 3 seconds
                        playDifferentSoundFile();
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


    private void playDifferentSoundFile() {
        Log.d(TAG, "Playing a different sound file");
        // Stop the current player
        stopTickPlayer();

        // Initialize and play a different sound file
        // Replace "yourDifferentSoundFile" with the appropriate resource/file
        MediaPlayer differentPlayer = MediaPlayer.create(this, R.raw.ticktoc2);

        if (differentPlayer != null) {
            differentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    // Resume the original tick player
                    playTickPlayer();
                }
            });

            differentPlayer.start();
        } else {
            Log.e(TAG, "Failed to create MediaPlayer for ticktoc2");
            // Handle the error, perhaps by using a default sound or notifying the user
        }
    }


    private void displayGameOverAndReturnToCategorySelection() {
        Log.d(TAG, "Entering displayGameOverAndReturnToCategorySelection");

        // Display "Game Over" using the string resource
        landscapeWordPairTextView.setText(R.string.game_over);

        Log.d(TAG, "Game Over message set");

        // Unregister the SensorEventListener to stop tilt detection
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
            Log.d(TAG, "SensorEventListener unregistered");
        } else {
            Log.e(TAG, "SensorManager is null");
        }

        // Display correct word count
        String correctWordsCountText = "Correct Words: " + correctWordCount;
        landscapeWordPairTextView.append("\n\n" + correctWordsCountText);

        // Display skipped words for 8 seconds
        if (!skippedWords.isEmpty()) {
            // Concatenate title and skipped words into a single string
            StringBuilder missedWordsText = new StringBuilder("Missed Words\n");
            for (String skippedWord : skippedWords) {
                missedWordsText.append(skippedWord).append("\n");
            }

            // Display all missed words at once
            landscapeWordPairTextView.append("\n\n" + missedWordsText.toString());
            Log.d(TAG, "Missed words displayed");

            // Delay for a moment before resetting the game and returning to category selection
            gameHandler.postDelayed(() -> {
                Log.d(TAG, "Resetting and starting new game");

                // Reset the game
                resetAndStartNewGame();

                // Build and show the AlertDialog
                showReturnToCategoryPrompt();
            }, 10000); // Delay for 10 seconds before showing the prompt
        } else {
            // No skipped words, reset the game and start a new one
            gameHandler.postDelayed(() -> {
                Log.d(TAG, "Resetting and starting new game (no skipped words)");

                resetAndStartNewGame();

                // Transition back to category selection
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                Log.d(TAG, "Returning to category selection");
            }, 5000); // Delay for 10 seconds before returning to category selection
        }

        Log.d(TAG, "Exiting displayGameOverAndReturnToCategorySelection");
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

    private void showReturnToCategoryPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Return to category selection?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User wants to return to category selection immediately
                    returnToCategorySelection();
                })
                .setCancelable(false); // User must choose an option

        AlertDialog dialog = builder.create();

        // Set dialog window parameters to position it at the top
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.TOP;

        dialog.show();
    }

    private void returnToCategorySelection() {
        // Transition back to category selection
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Finish the current activity
        Log.d(TAG, "Returning to category selection");
    }

    public void quitGame(View view) {
        // Show a confirmation dialog to confirm quitting the game
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to quit the game?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed, finish all activities and exit the app
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User canceled, do nothing or provide additional logic
                })
                .show();
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

