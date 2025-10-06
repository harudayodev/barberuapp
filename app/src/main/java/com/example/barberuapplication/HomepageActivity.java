package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
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

import android.content.SharedPreferences;
import java.io.InputStream;
import java.net.URL;

public class HomepageActivity extends AppCompatActivity {

    private ConstraintLayout servicesContainer;
    private ConstraintLayout toolsContainer;
    private TextView allcat, servicescat, toolscat;
    private TextView[] categoryTabs;
    private ImageView userProfileImage;

    @SuppressLint({"MissingInflatedId", "CutPasteId"})
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

        userProfileImage = findViewById(R.id.settingsIcon); // Display profile photo here

        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        usernameText.setText(fullname);

        loadUserProfile();

        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        ImageView apppointIcon = findViewById(R.id.appointlogo);
        ImageView hairstyleIcon = findViewById(R.id.haircutlogo);
        ImageView ratingIcon = findViewById(R.id.ratinglogo);
        ImageView homeIcon = findViewById(R.id.homeview);
        ImageView mapIcon = findViewById(R.id.mapslogo);
        ImageView favIcon = findViewById(R.id.favoritelogo);
        ImageView aboutIcon = findViewById(R.id.aboutlogo);
        ImageView historyIcon = findViewById(R.id.historylogo);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showLogoutDialog();
            }
        });

        logoutIcon.setOnClickListener(v -> showLogoutDialog());

        allcat.setOnClickListener(v -> showAll());
        servicescat.setOnClickListener(v -> showServices());
        toolscat.setOnClickListener(v -> showTools());

        showAll();

        settingsIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, Settings.class)));
        favIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, Favorites.class)));
        apppointIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, AppointmentActivity.class)));
        hairstyleIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, Camera.class)));
        ratingIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, Rating.class)));
        homeIcon.setOnClickListener(v -> recreate());
        mapIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, Maps.class)));
        aboutIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, About.class)));
        historyIcon.setOnClickListener(v -> startActivity(new Intent(HomepageActivity.this, History.class)));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(HomepageActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        TextView usernameText = findViewById(R.id.username);
        usernameText.setText(fullname);
        loadUserProfile();
    }

    /**
     * Updated selectCategory() to match HomepageAdmin behavior
     */
    private void selectCategory(TextView selectedTab) {
        Context context = this;
        int selectedTextColor = ContextCompat.getColor(context, R.color.white);
        int unselectedTextColor = ContextCompat.getColor(context, R.color.white);

        for (TextView tab : categoryTabs) {
            if (tab == selectedTab) {
                tab.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_orange_background));
                tab.setTextColor(selectedTextColor);
            } else {
                tab.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_transparent_background));
                tab.setTextColor(unselectedTextColor);
            }
            tab.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center text over background
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

    /**
     * Convert square bitmap to circular
     */
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
        String profileKey = "profilephoto_" + userId; // unique key per user
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
                        userProfileImage.setImageResource(R.drawable.iconuser); // default
                    }
                }
            }.execute(profileUrl);
        } else {
            userProfileImage.setImageResource(R.drawable.iconuser); // default
        }
    }
}
