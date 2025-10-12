package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class About extends AppCompatActivity {


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        ImageView returnbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        returnbutton.setOnClickListener(v -> finish());

        homebutton.setOnClickListener(v -> finish());

    }

}