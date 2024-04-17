package com.example.integrated;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FinalScore extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_score);

        TextView finalScoreTextView = findViewById(R.id.finalScoreTextView);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView finalScoreActivityNameTextView = findViewById(R.id.finalScoreActivityNameTextView); // Update layout accordingly
        Button restartButton = findViewById(R.id.restartButton);

        Intent intent = getIntent();
        String activityClassPath = intent.getStringExtra("activityClass");
        int cumulativeScore = intent.getIntExtra("cumulativeScore", 0);
        int totalTries = intent.getIntExtra("totalTries", 0);

        finalScoreTextView.setText(String.format("Final Score: %d/%d", cumulativeScore, totalTries));

        try {
            // Dynamically load class from the class path
            Class<?> activityClass = Class.forName(activityClassPath);
            String simpleName = activityClass.getSimpleName(); // Get a simple name of the class for display purposes
            finalScoreActivityNameTextView.setText(simpleName);

            restartButton.setOnClickListener(v -> {
                Intent restartIntent = new Intent(FinalScore.this, activityClass);
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                restartIntent.putExtra("reset", true);
                startActivity(restartIntent);
                finish();
            });

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // Handle the error or display an error message
        }
    }
}

