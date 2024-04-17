package com.example.integrated;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.integrated.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class Rhyme extends AppCompatActivity {
    private TextView levelNumberTextView, taskNumberTextView, wordTextView;
    private Button option1Button, option2Button, option3Button, option4Button;

    private int currentLevel = 1;
    private int currentTask = 1;
    private final int maxTasksPerLevel = 5;
    private int currentLevelScore = 0;
    private int currentLevelTries = 0;
    private int cumulativeScore = 0;
    private int totalTries = 0;

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
        editor.putInt("cumulativeScore", cumulativeScore);
        editor.putInt("totalTries", totalTries);
        editor.apply();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhyme);
        initializeUI();

        // Check if the activity is started with the intention to reset
        boolean shouldReset = getIntent().getBooleanExtra("reset", false);

        if (shouldReset) {
            // Optionally clear SharedPreferences
            SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            // Reset all progress to the initial state
            resetGameOrNavigate(); // Use your existing method to reset the game
        } else {
            // Load progress from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
            currentLevel = prefs.getInt("currentLevel", 1);
            currentTask = prefs.getInt("currentTask", 1);
            currentLevelScore = prefs.getInt("currentLevelScore", 0);
            currentLevelTries = prefs.getInt("currentLevelTries", 0);
            cumulativeScore = prefs.getInt("cumulativeScore", 0);
            totalTries = prefs.getInt("totalTries", 0);
        }

        updateUIForNewLevel();
        updateScoreDisplay();
        fetchRhymingTask(currentLevel);
    }

    private void resetGameOrNavigate() {
// Clear the saved state
        SharedPreferences prefs = getSharedPreferences("RhymeActivityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        // Navigate to the first level
        currentLevel = 1;
        currentTask = 1;
        currentLevelScore = 0;
        currentLevelTries = 0;
        cumulativeScore = 0;
        totalTries = 0;
    }


    private void initializeUI() {
        levelNumberTextView = findViewById(R.id.levelNumberTextView);
        taskNumberTextView = findViewById(R.id.taskNumberTextView);
        wordTextView = findViewById(R.id.wordTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        setupOptionButtons();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current state
        outState.putInt("currentLevel", currentLevel);
        outState.putInt("currentTask", currentTask);
        outState.putInt("currentLevelScore", currentLevelScore);
        outState.putInt("currentLevelTries", currentLevelTries);
        outState.putInt("cumulativeScore", cumulativeScore);
        outState.putInt("totalTries", totalTries);
    }

    private void setupOptionButtons() {
        View.OnClickListener optionClickListener = view -> {
            Button clickedButton = (Button) view;
            String selectedOption = clickedButton.getText().toString();
            validateOption(selectedOption, clickedButton); // Pass the clicked button as well
        };

        option1Button.setOnClickListener(optionClickListener);
        option2Button.setOnClickListener(optionClickListener);
        option3Button.setOnClickListener(optionClickListener);
        option4Button.setOnClickListener(optionClickListener);
    }


    private void validateOption(String selectedOption, Button clickedButton) {
        currentLevelTries++;
        totalTries++;
        String url = "http://172.16.53.98:5000/submit_rhyming_answer";

        try {
            JSONObject postData = new JSONObject();
            postData.put("word", wordTextView.getText().toString());
            postData.put("chosen_option", selectedOption);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> {
                        runOnUiThread(() -> {
                            try {
                                String result = response.getString("result");
                                if ("correct".equals(result)) {
                                    currentLevelScore++;
                                    cumulativeScore++;
                                    // Change to correct answer color
                                    ViewCompat.setBackgroundTintList(clickedButton, ColorStateList.valueOf(ContextCompat.getColor(Rhyme.this, R.color.correct_answer)));
                                } else {
                                    // Change to incorrect answer color
                                    ViewCompat.setBackgroundTintList(clickedButton, ColorStateList.valueOf(ContextCompat.getColor(Rhyme.this, R.color.incorrect_answer)));
                                }
                                // Wait for 2 seconds before proceeding, to let the user see the result
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    advanceTaskOrShowScore();
                                    // Reset the button tint to the default color
                                    ViewCompat.setBackgroundTintList(clickedButton, ColorStateList.valueOf(ContextCompat.getColor(Rhyme.this, R.color.button_default)));
                                }, 2000);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    },
                    error -> Toast.makeText(Rhyme.this, "Error validating answer", Toast.LENGTH_SHORT).show());

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Method to submit score
    private void submitScore(int userId, int level, int score) {
        String url = "http://172.16.53.98:5000/update_score";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("level", level);
            postData.put("score", score);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                    response -> {
                        // Handle response
                        Log.d("SubmitScore", "Score submitted successfully");
                    },
                    error -> {
                        // Handle error
                        Log.e("SubmitScore", "Error submitting score: " + error.getMessage());
                    });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("SubmitScore", "Error creating JSON object for submitting score", e);
        }
    }


    private void advanceTaskOrShowScore() {
        currentTask++;
        updateScoreDisplay();
        if (currentTask > maxTasksPerLevel) {
            showLevelScoreAndDecideNextStep();
        } else {
            taskNumberTextView.setText(String.valueOf(currentTask));
            fetchRhymingTask(currentLevel);
        }
    }

    private void showLevelScoreAndDecideNextStep() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userIdString = prefs.getString("userId", ""); // Retrieve as String
        int userId = -1; // Default to -1 or any other sentinel value indicating "not found" or "invalid"
        try {
            userId = Integer.parseInt(userIdString); // Convert String to Integer
        } catch (NumberFormatException e) {
            // Handle the case where userId is not a valid integer
            Log.e("Rhyme", "Invalid userId in SharedPreferences", e);
        }

        double scoreRatio = (double) currentLevelScore / currentLevelTries;
        String scoreMessage;

        // Check if the current level is the last level
        if (currentLevel == 3) {
            submitScore(userId, currentLevel, currentLevelScore);
            if (scoreRatio >= 0.8) {
                // Player passed the last level
                submitScore(userId, currentLevel, currentLevelScore);
                scoreMessage = String.format(Locale.getDefault(), "Your score for level %d: %d correct out of %d tries.\nCongratulations, you've completed all levels!", currentLevel, currentLevelScore, currentLevelTries);
            } else {
                // Player did not pass the last level
                scoreMessage = String.format(Locale.getDefault(), "Your score for level %d: %d correct out of %d tries.\nTry this level again to improve your score.", currentLevel, currentLevelScore, currentLevelTries);
            }
            // Show final results or option to retry the last level
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Level " + currentLevel + " Complete!");
            builder.setMessage(scoreMessage);
            builder.setPositiveButton("OK", (dialog, which) -> {
                if (scoreRatio >= 0.8) {
                    showFinalResults();
                } else {
                    restartCurrentLevel();
                }
            });
            builder.create().show();
        } else {
            submitScore(userId, currentLevel, currentLevelScore);
            // For levels 1 and 2
            if (scoreRatio >= 0.8) {
                // Player passed the level
                submitScore(userId, currentLevel, currentLevelScore);
                scoreMessage = String.format(Locale.getDefault(), "Your score for level %d: %d correct out of %d tries.\nCongratulations, you've advanced to the next level!", currentLevel, currentLevelScore, currentLevelTries);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Level " + currentLevel + " Complete!");
                builder.setMessage(scoreMessage);
                builder.setPositiveButton("Continue", (dialog, which) -> advanceToNextLevel());
                builder.create().show();
            } else {
                // Player did not pass the level
                scoreMessage = String.format(Locale.getDefault(), "Your score for level %d: %d correct out of %d tries.\nTry this level again to improve your score.", currentLevel, currentLevelScore, currentLevelTries);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Level " + currentLevel + " Complete!");
                builder.setMessage(scoreMessage);
                builder.setPositiveButton("Retry", (dialog, which) -> restartCurrentLevel());
                builder.create().show();
            }
        }
        // Reset current level scores for the next level or retry
        currentLevelScore = 0;
        currentLevelTries = 0;
    }


    private void advanceToNextLevel() {

        currentLevel++;
        currentTask = 1;
        // Reset the current level score and tries for the new level
        currentLevelScore = 0;
        currentLevelTries = 0;

        if (currentLevel > 3) {
            showFinalResults();
        } else {
            updateUIForNewLevel();
            updateScoreDisplay(); // Make sure this updates the score display to 0/0
        }
    }


    private void restartCurrentLevel() {
        currentTask = 1;
        // No need to increment currentLevel or cumulativeScore
        updateUIForNewLevel();
    }

    private void updateUIForNewLevel() {
        // Set the background color based on the current level
        LinearLayout rootView = findViewById(R.id.rootLayout);
        switch (currentLevel) {
            case 1:
                rootView.setBackgroundColor(getResources().getColor(R.color.level_one_background));
                break;
            case 2:
                rootView.setBackgroundColor(getResources().getColor(R.color.level_two_background));
                break;
            case 3:
                rootView.setBackgroundColor(getResources().getColor(R.color.level_three_background));
                break;
            default:
                // Default background color if needed
                break;
        }

        levelNumberTextView.setText(String.valueOf(currentLevel));
        taskNumberTextView.setText(String.valueOf(currentTask));
        fetchRhymingTask(currentLevel); // Fetch the first task of the new or retried level
        updateScoreDisplay(); // Ensure the score display is updated
    }


    private void updateScoreDisplay() {
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        String scoreText = "Score: " + currentLevelScore + "/" + currentLevelTries;
        scoreTextView.setText(scoreText);
    }


    private void showFinalResults() {
        clearSavedState(); // Clear the saved state before navigating to FinalScore
        Intent finalScoreIntent = new Intent(Rhyme.this, FinalScore.class);
        finalScoreIntent.putExtra("activityClass", Rhyme.class.getName());
        finalScoreIntent.putExtra("cumulativeScore", cumulativeScore);
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


    private void fetchRhymingTask(int level) {
        String url = "http://172.16.53.98:5000/get_rhyming_task/" + level; // Ensure this URL matches your server

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String word = response.getString("word");
                        JSONArray optionsArray = response.getJSONArray("options");
                        ArrayList<String> options = new ArrayList<>();
                        for (int i = 0; i < optionsArray.length(); i++) {
                            options.add(optionsArray.getString(i));
                        }

                        wordTextView.setText(word); // Set the word for the current task
                        // Assign options to buttons
                        option1Button.setText(options.get(0));
                        option2Button.setText(options.get(1));
                        option3Button.setText(options.get(2));
                        option4Button.setText(options.get(3));
                    } catch (JSONException e) {
                        Toast.makeText(Rhyme.this, "Failed to parse task data.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(Rhyme.this, "Error fetching the task.", Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
