package com.example.barberuapplication;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Reviews extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ImageView returnButton = findViewById(R.id.returnbutton);
        ImageView homeButton = findViewById(R.id.homeview);

        // Only keep return and home functionality
        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());
    }
}
