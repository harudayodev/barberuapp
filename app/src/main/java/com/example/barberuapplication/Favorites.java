package com.example.barberuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Favorites extends AppCompatActivity {

    ImageView returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        ImageView retbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        retbutton.setOnClickListener(v -> finish());

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(Favorites.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

    }
}