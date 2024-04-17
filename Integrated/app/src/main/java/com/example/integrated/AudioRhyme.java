package com.example.integrated;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import com.airbnb.lottie.LottieAnimationView;
import com.example.integrated.R;

public class AudioRhyme extends AppCompatActivity {
    private static final String TAG = "AudioRhyme";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int MAX_LEVELS = 3;
    private static final int TASKS_PER_LEVEL = 5;

    private TextView wordTextView, levelTextView, taskTextView, scoreTextView;
    private Button recordButton, submitButton, btnPlay;

    private MediaPlayer successSound;
    private MediaPlayer failureSound;
    private LottieAnimationView animationView;

    private int currentLevel = 1;
    private int currentTask = 1;
    private int correctTasks = 0;
    private int totalTries = 0;
    private int currentLevelScore = 0;
    private int currentLevelTries = 0;

    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    private boolean isRecording = false;

    public interface AnimationCompleteListener {
        void onAnimationComplete();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_rhyme);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Initialize media players
        successSound = MediaPlayer.create(this, R.raw.success);
        failureSound = MediaPlayer.create(this, R.raw.failure);

        // Initialize the animation view from your layout
        animationView = findViewById(R.id.animation_view);

        initUI();

        // Check if the activity is started with the intention to reset
        boolean shouldReset = getIntent().getBooleanExtra("reset", false);

        if (shouldReset) {
            // Optionally clear SharedPreferences
            SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            // Reset all progress to the initial state
            resetGameOrNavigate(); // Reset game state and UI
        } else {

            // Load progress from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
            currentLevel = prefs.getInt("currentLevel", 1);
            currentTask = prefs.getInt("currentTask", 1);
            currentLevelScore = prefs.getInt("currentLevelScore", 0);
            currentLevelTries = prefs.getInt("currentLevelTries", 0);
            correctTasks = prefs.getInt("cumulativeScore", 0);
            totalTries = prefs.getInt("totalTries", 0);
            // Regular game startup logic

            updateUIForNewLevel();
            updateScoreDisplay();

            fetchWordAndOptions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Saving state when the activity is paused
        SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("currentLevel", currentLevel);
        editor.putInt("currentTask", currentTask);
        editor.putInt("currentLevelScore", currentLevelScore);
        editor.putInt("currentLevelTries", currentLevelTries);
        editor.putInt("cumulativeScore", correctTasks);
        editor.putInt("totalTries", totalTries);
        editor.apply();
    }

    private void resetGameOrNavigate() {
        // Reset all game state
        currentLevel = 1;
        currentTask = 1;
        currentLevelScore = 0;
        currentLevelTries = 0;
        correctTasks = 0;
        totalTries = 0;

        // Clear any saved preferences if used for state persistence
        SharedPreferences prefs = getSharedPreferences("WordHarmonyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Reset UI components to initial state
        initUI();

        // Start the game at the first level
        fetchWordAndOptions();
    }


    private void updateScoreDisplay() {
        String scoreText = "Score: " + currentLevelScore + "/" + currentLevelTries;
        scoreTextView.setText(scoreText);
    }

    private void initUI() {
        wordTextView = findViewById(R.id.wordTextView);
        levelTextView = findViewById(R.id.levelTextView);
        taskTextView = findViewById(R.id.taskTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        recordButton = findViewById(R.id.recordButton);
        submitButton = findViewById(R.id.submitButton);
        btnPlay = findViewById(R.id.btnPlay);

        recordButton.setOnClickListener(view -> toggleRecording());
        submitButton.setOnClickListener(view -> submitAudio());
        btnPlay.setOnClickListener(view -> playRecordedAudio());

        updateLevelTaskUI();
    }

    private void toggleRecording() {
        if (recorder != null) {
            stopRecording();
            recordButton.setText(R.string.start_recording);
        } else {
            startRecording();
            recordButton.setText(R.string.stop_recording);
        }
    }

    private void startRecording() {
        fileName = getExternalFilesDir(null).getAbsolutePath() + "/audioRecord.3gp";

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed", e);
        }
    }

//    private void stopRecording() {
//        recorder.stop();
//        recorder.release();
//        recorder = null;
//        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
//    }

    private void stopRecording() {
        // Check if the recorder is not null
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (IllegalStateException e) {
                // Handle the IllegalStateException
                Log.e(TAG, "Error stopping the recorder", e);
            } finally {
                recorder = null;
            }
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void playRecordedAudio() {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
        } catch (IOException e) {
            Log.e(TAG, "Could not play the recorded audio", e);
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void fetchWordAndOptions() {
        String url = "http://172.16.53.98:5000/audio_rhyme/get_word?level=" + currentLevel;
        Log.d(TAG, "Fetching word and options for Level: " + currentLevel + ", Task: " + currentTask);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Response received: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String word = jsonResponse.getString("word");
                        JSONArray optionsArray = jsonResponse.getJSONArray("options");
                        wordTextView.setText(word);

                        if(optionsArray.length() > 0) {
                            for (int i = 0; i < optionsArray.length(); i++) {
                                String option = optionsArray.getString(i);
                                switch (i) {
                                    case 0:
                                        ((Button)findViewById(R.id.option1Button)).setText(option);
                                        break;
                                    case 1:
                                        ((Button)findViewById(R.id.option2Button)).setText(option);
                                        break;
                                    case 2:
                                        ((Button)findViewById(R.id.option3Button)).setText(option);
                                        break;
                                    case 3:
                                        ((Button)findViewById(R.id.option4Button)).setText(option);
                                        break;
                                }
                            }
                        }

                        updateLevelTaskUI(); // Call this method to update UI elements for level and task
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to fetch word and options", Toast.LENGTH_SHORT).show();
                    }

                },

                error -> Toast.makeText(this, "Error fetching word and options: " + error.toString(), Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void submitAudio() {
        String url = "http://172.16.53.98:5000/audio_rhyme/submit?level=" + currentLevel + "&task=" + currentTask;
        Log.d(TAG, "Submitting audio for Level: " + currentLevel + ", Task: " + currentTask);

        Log.d(TAG, "File name: " + fileName);

        File audioFile = new File(fileName);

        if (!audioFile.exists() || audioFile.isDirectory()) {
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "File does not exist: " + fileName);
            return;
        }

        Response.Listener<String> responseListener = response -> {
            Log.d(TAG, "Submission response: " + response);
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean isCorrectRhyme = jsonResponse.getBoolean("is_correct_rhyme");
                String message = isCorrectRhyme ? "Correct rhyme!" : "Incorrect rhyme.";
                Toast.makeText(AudioRhyme.this, message, Toast.LENGTH_SHORT).show();
                handleTaskResult(isCorrectRhyme);
            } catch (JSONException e) {
                Toast.makeText(AudioRhyme.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
            }
        };

        Response.ErrorListener errorListener = error -> Toast.makeText(AudioRhyme.this, "Upload error: " + error.toString(), Toast.LENGTH_SHORT).show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(url, wordTextView.getText().toString(), audioFile, responseListener, errorListener);

        Volley.newRequestQueue(this).add(multipartRequest);
    }


    private void handleTaskResult(boolean isCorrect) {
        currentLevelTries++;
        totalTries++;

        if (isCorrect) {
            correctTasks++;
            currentLevelScore++;
        }

        updateScoreDisplay(); // Update the score display

        if (currentTask == TASKS_PER_LEVEL) {
            playSoundAndAnimation(isCorrect, new AnimationCompleteListener() {
                @Override
                public void onAnimationComplete() {
                    if (isCorrect) {
                        if (currentLevel == MAX_LEVELS) {
                            showFinalResults(); // If it's the last level, show final results
                        } else {
                            currentLevel++; // Move to the next level
                            currentTask = 1; // Reset tasks for the new level
                            currentLevelScore = 0; // Reset score for the new level
                            currentLevelTries = 0; // Reset tries for the new level
                            fetchWordAndOptions(); // Fetch new words and options for the new level
                            updateLevelTaskUI(); // Update the UI for the new level
                        }
                    } else {
                        currentTask = 1; // Reset task count for retrying the level
                        currentLevelScore = 0; // Reset score for retrying the level
                        currentLevelTries = 0; // Reset tries for retrying the level
                        fetchWordAndOptions(); // Fetch the same words and options for retrying the level
                        updateLevelTaskUI(); // Update the UI for retrying the level
                    }
                }
            });
        } else {
            currentTask++; // Move to the next task within the same level
            fetchWordAndOptions();
        }
    }





//    private void handleTaskResult(boolean isCorrect) {
//        currentLevelTries++;
//        totalTries++;
//
//        if (isCorrect) {
//            correctTasks++;
//            currentLevelScore++;
//        }
//
//        if (currentTask == TASKS_PER_LEVEL) {
//
//            // Submit the score regardless of being correct or incorrect
//            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
//            String userId = prefs.getString("userId", "");
//            if (!userId.isEmpty()) {
//                int parsedUserId = Integer.parseInt(userId); // Ensure userId is correctly parsed as an int
//                submitScore(parsedUserId, currentLevel, currentLevelScore);
//            }
//
//            showLevelScoreAndDecideNextStep();
//        } else {
//            currentTask++;
//            fetchWordAndOptions();
//        }
//        handleResponse(isCorrect, submitButton);
//    }

    private void submitScore(int userId, int level, int score) {
        String url = "http://172.16.53.98:5000/submit_audio_rhyme_score"; // Adjust the URL as needed
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("level", level);
            postData.put("score", score);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> Log.d(TAG, "Score submitted successfully"),
                    error -> Log.e(TAG, "Error submitting score: " + error.getMessage()));

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for score submission", e);
        }
    }


    private void handleResponse(boolean isCorrect, Button button) {
        int colorId = isCorrect ? R.color.correct_answer : R.color.incorrect_answer;
        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), colorId));

        new Handler().postDelayed(() -> button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.button_default)), 2000);
    }

    private void showLevelScoreAndDecideNextStep() {
        double scoreRatio = (double) currentLevelScore / currentLevelTries;
        boolean isSuccess = scoreRatio >= 0.4; // Assuming 0.5 is the threshold for success

        playSoundAndAnimation(isSuccess, new AnimationCompleteListener() {
            @Override
            public void onAnimationComplete() {
                if (isSuccess) {
                    if (currentLevel == MAX_LEVELS) {
                        showFinalResults(); // Show final results if it was the last level
                    } else {
                        resetForNewLevel(); // Move to the next level
                    }
                } else {
                    retryCurrentLevel(); // Retry the current level if not successful
                }
            }
        });
    }


//    private void showLevelScoreAndDecideNextStep() {
//        AlertDialog.Builder builder =new AlertDialog.Builder(this);
//        double scoreRatio = (double) currentLevelScore / currentLevelTries;
//
//        String message = "Level " + currentLevel + " completed.\nYour score: " + currentLevelScore + "/" + currentLevelTries;
//        if (scoreRatio >= 0.2) {
//            if (currentLevel == MAX_LEVELS) {
//                message += "\nCongratulations! You've completed all levels.";
//                builder.setPositiveButton("Finish", (dialog, which) -> showFinalResults());
//            } else {
//                currentLevel++;
//                message += "\nMoving to the next level!";
//                builder.setPositiveButton("Next Level", (dialog, which) -> resetForNewLevel());
//            }
//        } else {
//            message += "\nTry again to improve your score.";
//            builder.setPositiveButton("Retry Level", (dialog, which) -> retryCurrentLevel());
//        }
//
//        builder.setMessage(message);
//        builder.setCancelable(false);
//        builder.show();
//    }

    private void playSoundAndAnimation(boolean isSuccess, AnimationCompleteListener listener) {
        MediaPlayer mediaPlayer = isSuccess ? successSound : failureSound;
        String animationFile = isSuccess ? "success_animation.json" : "failure_animation.json";

        mediaPlayer.start(); // Play the appropriate sound
        animationView.setAnimation(animationFile); // Set the appropriate animation
        animationView.playAnimation();
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationComplete(); // Call the listener once the animation ends
                animationView.removeAllAnimatorListeners(); // Remove listeners to avoid memory leaks
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }



    private void resetForNewLevel() {
        currentTask = 1;
        currentLevelScore = 0;
        currentLevelTries = 0;
        fetchWordAndOptions();
        updateLevelTaskUI();
    }

    private void retryCurrentLevel() {
        currentTask = 1;
        currentLevelScore = 0;
        currentLevelTries = 0;
        fetchWordAndOptions();
        updateLevelTaskUI();
    }

    private void showFinalResults() {
        clearSavedState(); // Clear the saved state before navigating to FinalScore
        Intent finalScoreIntent = new Intent(AudioRhyme.this, FinalScore.class);
        finalScoreIntent.putExtra("activityClass", AudioRhyme.class.getName());
        finalScoreIntent.putExtra("cumulativeScore", correctTasks);
        finalScoreIntent.putExtra("totalTries", totalTries);
        startActivity(finalScoreIntent);
        finish(); // Optionally, close the current activity
    }

    private void clearSavedState() {
        SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // This will clear the saved state
        editor.apply();
    }

    private void updateLevelTaskUI() {
        levelTextView.setText(String.format(Locale.getDefault(), "%d", currentLevel));
        taskTextView.setText(String.format(Locale.getDefault(), "%d", currentTask));
        // Display current level score and tries, not cumulative
        scoreTextView.setText(String.format(Locale.getDefault(), "Score: %d/%d", currentLevelScore, currentLevelTries));
        updateUIForNewLevel(); // Add this line
    }

    private void updateUIForNewLevel() {
        LinearLayout rootLayout = findViewById(R.id.rootLayout); // Assuming this is your root layout
        switch (currentLevel) {
            case 1:
                rootLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.level_one_background));
                break;
            case 2:
                rootLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.level_two_background));
                break;
            case 3:
                rootLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.level_three_background));
                break;
            default:
                // Optional: Default background color if needed
                break;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, initialize anything that required permission.
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (successSound != null) {
            successSound.release();
        }
        if (failureSound != null) {
            failureSound.release();
        }
        stopRecording();
        releaseMediaPlayer();
    }
}