package com.example.barberuapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminQueue extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminqueue);

        ImageView returnButton = findViewById(R.id.return_button);
        ImageView homeButton = findViewById(R.id.homeview);

        // Only keep return and home functionality
        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());
    }
}
