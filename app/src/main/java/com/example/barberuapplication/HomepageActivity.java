package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;

public class HomepageActivity extends AppCompatActivity {

    private ConstraintLayout servicesContainer;
    private ConstraintLayout toolsContainer;
    private TextView allcat, servicescat, toolscat;
    private TextView[] categoryTabs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        ImageView logoutIcon = findViewById(R.id.logoutbutton);
        TextView usernameText = findViewById(R.id.username);

        allcat = findViewById(R.id.allcat);
        servicescat = findViewById(R.id.servicescat);
        toolscat = findViewById(R.id.toolscat);
        servicesContainer = findViewById(R.id.services_container);
        toolsContainer = findViewById(R.id.tools_container);

        categoryTabs = new TextView[]{allcat, servicescat, toolscat};

        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        usernameText.setText(fullname);

        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        ImageView apppointIcon = findViewById(R.id.appointlogo);
        ImageView hairstyleIcon = findViewById(R.id.haircutlogo);
        ImageView reviewIcon = findViewById(R.id.ratinglogo);
        ImageView homeIcon = findViewById(R.id.homeview);
        ImageView mapIcon = findViewById(R.id.mapslogo);
        ImageView favIcon = findViewById(R.id.favoritelogo);
        ImageView aboutIcon = findViewById(R.id.aboutlogo);
        ImageView historyIcon = findViewById(R.id.historylogo);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomepageActivity.this)
                        .setTitle("Logout and Exit")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        logoutIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(HomepageActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        allcat.setOnClickListener(v -> showAll());
        servicescat.setOnClickListener(v -> showServices());
        toolscat.setOnClickListener(v -> showTools());

        showAll();

        settingsIcon.setOnClickListener(v -> {
            Intent settingview = new Intent(HomepageActivity.this, Settings.class);
            startActivity(settingview);
            settingview.putExtra("role", "user");
        });

        favIcon.setOnClickListener(v -> {
            Intent favview = new Intent(HomepageActivity.this, Favorites.class);
            startActivity(favview);
        });

        apppointIcon.setOnClickListener(v -> {
            Intent queueview = new Intent(HomepageActivity.this, AppointmentActivity.class);
            startActivity(queueview);
        });

        hairstyleIcon.setOnClickListener(v -> {
            Intent hairstyleview = new Intent(HomepageActivity.this, CameraActivity.class);
            startActivity(hairstyleview);
        });

        reviewIcon.setOnClickListener(v -> {
            Intent ratingview = new Intent(HomepageActivity.this, ReviewActivity.class);
            startActivity(ratingview);
        });

        homeIcon.setOnClickListener(v -> recreate());

        mapIcon.setOnClickListener(v -> {
            Intent mapview = new Intent(HomepageActivity.this, Maps.class);
            mapview.putExtra("role", "user");
            startActivity(mapview);
        });

        aboutIcon.setOnClickListener((v -> {
            Intent aboutview = new Intent (HomepageActivity.this, About.class);
            aboutview.putExtra("role", "user");
            startActivity(aboutview);
        }));

        historyIcon.setOnClickListener((v -> {
            Intent historyview = new Intent(HomepageActivity.this, History.class);
            startActivity(historyview);
        }));
    }

    protected void onResume() {
        super.onResume();
        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");

        TextView usernameText = findViewById(R.id.username);
        usernameText.setText(fullname);
    }


    private void selectCategory(TextView selectedTab) {
        Context context = this;
        Drawable selectedBg = ContextCompat.getDrawable(context, R.drawable.rounded_orange_background);
        Drawable unselectedBg = ContextCompat.getDrawable(context, R.drawable.rounded_transparent_background);
        int selectedTextColor = ContextCompat.getColor(context, R.color.hotorange);
        int unselectedTextColor = ContextCompat.getColor(context, R.color.white);

        for (TextView tab : categoryTabs) {
            if (tab == selectedTab) {
                tab.setBackground(selectedBg);
                tab.setTextColor(unselectedTextColor);
            } else {
                tab.setBackground(unselectedBg);
                tab.setTextColor(unselectedTextColor);
            }
        }
    }

    private void showAll() {
        servicesContainer.setVisibility(View.VISIBLE);
        toolsContainer.setVisibility(View.VISIBLE);
        selectCategory(allcat);
    }

    private void showServices() {
        servicesContainer.setVisibility(View.VISIBLE);
        toolsContainer.setVisibility(View.GONE);
        selectCategory(servicescat);
    }

    private void showTools() {
        servicesContainer.setVisibility(View.GONE);
        toolsContainer.setVisibility(View.VISIBLE);
        selectCategory(toolscat);
    }
}