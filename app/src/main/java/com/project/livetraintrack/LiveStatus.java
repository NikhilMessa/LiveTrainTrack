package com.project.livetraintrack;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LiveStatus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        // Retrieve the train status information from the Intent
        String trainStatus = getIntent().getStringExtra("trainStatus");

        // Display the train status in a TextView
        TextView trainStatusTextView = findViewById(R.id.trainStatusTextView);
        trainStatusTextView.setText(trainStatus);
    }
}
