package com.example.barberuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);

        ImageView returnButton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        returnButton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            Intent intent;
            if ("admin".equals(role)) {
                intent = new Intent(ReviewActivity.this, HomepageAdmin.class);
            } else {
                intent = new Intent(ReviewActivity.this, HomepageActivity.class);
            }
            startActivity(intent);
            finish();
        });

        homebutton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            Intent intent;
            if ("admin".equals(role)) {
                intent = new Intent(ReviewActivity.this, HomepageAdmin.class);
            } else {
                intent = new Intent(ReviewActivity.this, HomepageActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}