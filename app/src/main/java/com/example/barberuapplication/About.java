package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class About extends AppCompatActivity {


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        ImageView returnbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        returnbutton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            if ("admin".equals(role)) {
                Intent intent = new Intent(About.this, HomepageAdmin.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(About.this, HomepageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        homebutton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            if ("admin".equals(role)) {
                Intent intent = new Intent(About.this, HomepageAdmin.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(About.this, HomepageActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}