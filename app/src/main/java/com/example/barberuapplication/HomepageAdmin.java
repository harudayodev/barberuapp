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

public class HomepageAdmin extends AppCompatActivity {

    private ConstraintLayout toolsContainer;
    private TextView allcat, toolscat;
    private TextView[] categoryTabs;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepageadmin);

        ImageView logoutIcon = findViewById(R.id.logoutbutton);
        TextView shopNameText = findViewById(R.id.shopname);
        TextView usernameText = findViewById(R.id.username);

        allcat = findViewById(R.id.allcat);
        toolscat = findViewById(R.id.toolscat);
        toolsContainer = findViewById(R.id.tools_container);
        categoryTabs = new TextView[]{allcat, toolscat};

        // ✅ Get fullname
        String fullname = getIntent().getStringExtra("fullname");
        if (fullname == null || fullname.trim().isEmpty()) {
            fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("fullname", "User");
        }
        usernameText.setText(fullname);

        // ✅ Get shop name directly from login response (or SharedPreferences)
        String shopName = getIntent().getStringExtra("shop_name");
        if (shopName == null || shopName.trim().isEmpty()) {
            shopName = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("shop_name", "");
        }
        if (shopName != null && !shopName.isEmpty()) {
            shopNameText.setText("of " + shopName);
        } else {
            shopNameText.setText("");
        }

        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        ImageView queueIcon = findViewById(R.id.queuelogo);
        ImageView feedbackIcon = findViewById(R.id.feedbacklogo);
        ImageView homeIcon = findViewById(R.id.homeview);
        ImageView mapIcon = findViewById(R.id.maplogo);
        ImageView aboutIcon = findViewById(R.id.aboutlogo);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomepageAdmin.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(HomepageAdmin.this, MainActivity.class);
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
            new AlertDialog.Builder(HomepageAdmin.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(HomepageAdmin.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // ✅ Category tabs
        allcat.setOnClickListener(v -> showAll());
        toolscat.setOnClickListener(v -> showTools());
        showAll();

        String finalFullname = fullname;
        settingsIcon.setOnClickListener(v -> {
            Intent settingview = new Intent(HomepageAdmin.this, Settings.class);
            settingview.putExtra("fullname", finalFullname);
            settingview.putExtra("role", "admin");
            startActivity(settingview);
        });

        queueIcon.setOnClickListener(v -> {
            Intent queueview = new Intent(HomepageAdmin.this, AdminQueue.class);
            queueview.putExtra("role", "admin");
            startActivity(queueview);
        });

        feedbackIcon.setOnClickListener(v -> {
            Intent ratingview = new Intent(HomepageAdmin.this, ReviewActivity.class);
            ratingview.putExtra("role", "admin");
            startActivity(ratingview);
        });

        homeIcon.setOnClickListener(v -> recreate());

        mapIcon.setOnClickListener(v -> {
            Intent mapview = new Intent(HomepageAdmin.this, Maps.class);
            mapview.putExtra("role", "admin");
            startActivity(mapview);
        });

        aboutIcon.setOnClickListener(v -> {
            Intent aboutview = new Intent(HomepageAdmin.this, About.class);
            aboutview.putExtra("role", "admin");
            startActivity(aboutview);
        });
    }

    private void selectCategory(TextView selectedTab) {
        Context context = this;
        Drawable selectedBg = ContextCompat.getDrawable(context, R.drawable.rounded_orange_background);
        Drawable unselectedBg = ContextCompat.getDrawable(context, R.drawable.rounded_transparent_background);
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
        toolsContainer.setVisibility(View.VISIBLE);
        selectCategory(allcat);
    }

    private void showTools() {
        toolsContainer.setVisibility(View.VISIBLE);
        selectCategory(toolscat);
    }
}
