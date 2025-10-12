package com.example.barberuapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminQueue extends AppCompatActivity {

    /** @noinspection FieldCanBeLocal*/
    private int employeeID = 0; // Logged-in employee ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminqueue);

        // Get employeeID from intent or SharedPreferences
        employeeID = getIntent().getIntExtra("employeeID", 0);
        if (employeeID == 0) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            employeeID = prefs.getInt("employee_id", 0);
        }

        ImageView returnButton = findViewById(R.id.return_button);
        ImageView homeButton = findViewById(R.id.homeview);

        // Only keep return and home functionality
        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());
    }
}
