package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.net.URL;

public class HomepageAdmin extends AppCompatActivity {

    private ConstraintLayout toolsContainer;
    private TextView allcat, toolscat;
    private TextView[] categoryTabs;
    private ImageView userProfileImage;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepageadmin);

        ImageView logoutIcon = findViewById(R.id.logoutbutton);
        TextView shopNameText = findViewById(R.id.shopname);
        TextView usernameText = findViewById(R.id.username);

        userProfileImage = findViewById(R.id.settingsIcon);

        allcat = findViewById(R.id.allcat);
        toolscat = findViewById(R.id.toolscat);
        toolsContainer = findViewById(R.id.tools_container);
        categoryTabs = new TextView[]{allcat, toolscat};

        // ✅ Load fullname
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fullname = prefs.getString("fullname", "User");
        usernameText.setText(fullname);

        // ✅ Load shop name
        String shopName = prefs.getString("shop_name", "");
        shopNameText.setText(shopName.isEmpty() ? "" : "of " + shopName);

        // ✅ Load profile photo
        loadUserProfile();

        @SuppressLint("CutPasteId") ImageView settingsIcon = findViewById(R.id.settingsIcon);
        ImageView queueIcon = findViewById(R.id.queuelogo);
        ImageView feedbackIcon = findViewById(R.id.feedbacklogo);
        ImageView homeIcon = findViewById(R.id.homeview);
        ImageView mapIcon = findViewById(R.id.maplogo);
        ImageView calenderIcon = findViewById(R.id.calendarlogo);
        ImageView aboutIcon = findViewById(R.id.aboutlogo);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutDialog();
            }
        });

        logoutIcon.setOnClickListener(v -> showLogoutDialog());

        // ✅ Category tabs
        allcat.setOnClickListener(v -> showAll());
        toolscat.setOnClickListener(v -> showTools());
        showAll();

        // ----------------- BUTTON CLICK LISTENERS -----------------
        settingsIcon.setOnClickListener(v -> {
            Intent settingview = new Intent(HomepageAdmin.this, Settings.class);
            settingview.putExtra("fullname", fullname);
            settingview.putExtra("role", "admin");
            startActivity(settingview);
        });

        queueIcon.setOnClickListener(v -> {
            int employeeID = prefs.getInt("employee_id", 0); // ✅ Correct key
            Intent queueview = new Intent(HomepageAdmin.this, AdminQueue.class);
            queueview.putExtra("role", "admin");
            queueview.putExtra("employeeID", employeeID);
            startActivity(queueview);
        });

        feedbackIcon.setOnClickListener(v -> {
            Intent ratingview = new Intent(HomepageAdmin.this, Reviews.class);
            ratingview.putExtra("role", "admin");
            startActivity(ratingview);
        });

        homeIcon.setOnClickListener(v -> recreate());

        mapIcon.setOnClickListener(v -> {
            Intent mapview = new Intent(HomepageAdmin.this, Maps.class);
            mapview.putExtra("role", "admin");
            startActivity(mapview);
        });

        calenderIcon.setOnClickListener(v -> {
            String loggedInEmail = prefs.getString("email", "");
            Intent calenderview = new Intent(HomepageAdmin.this, AdminCalendar.class);
            calenderview.putExtra("role", "admin");
            calenderview.putExtra("email", loggedInEmail);
            startActivity(calenderview);
        });

        aboutIcon.setOnClickListener(v -> {
            Intent aboutview = new Intent(HomepageAdmin.this, About.class);
            aboutview.putExtra("role", "admin");
            startActivity(aboutview);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload fullname and profile photo
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fullname = prefs.getString("fullname", "User");
        TextView usernameText = findViewById(R.id.username);
        usernameText.setText(fullname);
        loadUserProfile();
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

    private void showLogoutDialog() {
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
    }

    // ------------------- PROFILE PHOTO -------------------
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }

    @SuppressLint("StaticFieldLeak")
    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userID", 0);
        String profileKey = "profilephoto_" + userId;
        String profileUrl = prefs.getString(profileKey, "");

        if (!profileUrl.isEmpty()) {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... urls) {
                    try {
                        InputStream in = new URL(urls[0]).openStream();
                        Bitmap bmp = BitmapFactory.decodeStream(in);
                        return getCircularBitmap(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        userProfileImage.setImageBitmap(bitmap);
                    } else {
                        userProfileImage.setImageResource(R.drawable.iconuser);
                    }
                }
            }.execute(profileUrl);
        } else {
            userProfileImage.setImageResource(R.drawable.iconuser);
        }
    }
}
