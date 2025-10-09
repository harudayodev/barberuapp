package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import android.util.Base64;

public class Settings extends AppCompatActivity {

    private TextView usernameText;
    private ImageView userProfileImage;
    /** @noinspection FieldCanBeLocal*/
    private View shareAppContainer; // New: container for Share App
    private int userId;
    private static final int PICK_IMAGE_REQUEST = 1001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        usernameText = findViewById(R.id.user_name);
        userProfileImage = findViewById(R.id.userprofile);
        shareAppContainer = findViewById(R.id.share_app_container); // initialize

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userID", 0);
        String fullname = prefs.getString("fullname", "User");
        usernameText.setText(fullname);

        findViewById(R.id.returnb).setOnClickListener(v -> finish());

        findViewById(R.id.change_profile_btn).setOnClickListener(v -> openImagePicker());

        // Edit profile
        ImageView editProfile = findViewById(R.id.editprofile);
        editProfile.setOnClickListener(v -> showEditDialog());

        View signOutContainer = findViewById(R.id.sign_out_container);
        View signOutText = findViewById(R.id.sign_out_text);
        View signOutIcon = findViewById(R.id.sign_out_icon);

        View.OnClickListener logoutListener = v -> showLogoutDialog();
        signOutContainer.setOnClickListener(v -> showLogoutDialog());

        // Change password
        findViewById(R.id.change_password_container).setOnClickListener(v -> showChangePasswordDialog());

        // Share App
        if (shareAppContainer != null) {
            shareAppContainer.setOnClickListener(v -> shareApp());
        }

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        usernameText.setText(fullname);
        loadUserProfile();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(Settings.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear session data
                    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();

                    // Redirect to login page
                    Intent intent = new Intent(Settings.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                    // Display logout success message
                    Toast.makeText(Settings.this, "You have been logged out successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    /** -------------------- SHARE APP -------------------- */
    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            String subject = "Check out BarberU!";
            String shareMessage = "Hey, I've been using BarberU to visualize new hairstyles! " +
                    "You should try it too.\n\n[Insert Google Play Store Link Here]";

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            startActivity(Intent.createChooser(shareIntent, "Share BarberU via"));
        } catch (Exception e) {
            Toast.makeText(this, "Sharing failed.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /** -------------------- PROFILE IMAGE METHODS -------------------- */
    @SuppressLint("IntentReset")
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newSize = Math.min(width, height);
                Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, (width - newSize)/2, (height - newSize)/2, newSize, newSize);

                float scale = getResources().getDisplayMetrics().density;
                int targetSize = (int)(120 * scale);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true);

                showConfirmDialog(scaledBitmap);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConfirmDialog(Bitmap bitmap) {
        ImageView preview = new ImageView(this);
        Bitmap circularPreview = getCircularBitmap(bitmap);
        preview.setImageBitmap(circularPreview);
        int padding = (int)(24 * getResources().getDisplayMetrics().density);
        preview.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Set as Profile Photo?")
                .setView(preview)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    userProfileImage.setImageBitmap(circularPreview);
                    uploadProfileImage(bitmap);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

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
    private void uploadProfileImage(Bitmap bitmap) {
        new AsyncTask<Bitmap, Void, String>() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            protected String doInBackground(Bitmap... bitmaps) {
                try {
                    Bitmap bmp = bitmaps[0];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    URL url = new URL(Config.BASE_URL + "upload_profile.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String data = URLEncoder.encode("id", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8) + "&" +
                            URLEncoder.encode("profile", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(encodedImage, StandardCharsets.UTF_8);

                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes());
                    os.flush();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        Toast.makeText(Settings.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        if (json.getString("status").equals("success")) {
                            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                            String profileKey = "profilephoto_" + userId;
                            editor.putString(profileKey, json.getString("url"));
                            editor.apply();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Settings.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bitmap);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Store profile photo separately per user
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
                        userProfileImage.setImageResource(R.drawable.iconuser); // default
                    }
                }
            }.execute(profileUrl);
        } else {
            userProfileImage.setImageResource(R.drawable.iconuser); // default
        }
    }

    /** -------------------- EDIT PROFILE METHODS -------------------- */
    private void showEditDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final android.view.View dialogView = inflater.inflate(R.layout.dialog_change_username, null);
        EditText firstNameInput = dialogView.findViewById(R.id.edit_firstname);
        EditText lastNameInput = dialogView.findViewById(R.id.edit_lastname);

        String[] names = usernameText.getText().toString().split(" ", 2);
        if (names.length > 0) firstNameInput.setText(names[0]);
        if (names.length > 1) lastNameInput.setText(names[1]);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String newFirst = firstNameInput.getText().toString().trim();
                    String newLast = lastNameInput.getText().toString().trim();
                    if (newFirst.isEmpty()) {
                        Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
                    } else {
                        updateProfile(newFirst, newLast);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateProfile(String firstname, String lastname) {
        if (userId <= 0) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }
        new AsyncTask<Void, Void, String>() {
            /** @noinspection CharsetObjectCanBeUsed*/
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "editprofile.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    String data = URLEncoder.encode("id", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8.name()) +
                            "&" + URLEncoder.encode("firstname", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(firstname, StandardCharsets.UTF_8.name()) +
                            "&" + URLEncoder.encode("lastname", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(lastname, StandardCharsets.UTF_8.name());
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        if (json.getString("status").equals("success")) {
                            String fullname = lastname.isEmpty() ? firstname : firstname + " " + lastname;
                            usernameText.setText(fullname);
                            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("fullname", fullname)
                                    .apply();
                            Toast.makeText(Settings.this, json.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Settings.this, json.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(Settings.this, "Update failed: Invalid server response.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Settings.this, "Network error: Could not connect to server.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /** -------------------- CHANGE PASSWORD METHODS -------------------- */
    private void showChangePasswordDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        EditText currentPassword = dialogView.findViewById(R.id.current_password);
        EditText newPassword = dialogView.findViewById(R.id.new_password);
        EditText confirmPassword = dialogView.findViewById(R.id.confirm_password);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String current = currentPassword.getText().toString().trim();
                    String newPass = newPassword.getText().toString().trim();
                    String confirm = confirmPassword.getText().toString().trim();

                    if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    } else if (!newPass.equals(confirm)) {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    } else {
                        updatePassword(current, newPass);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private void updatePassword(String current, String newPass) {
        new AsyncTask<Void, Void, String>() {
            /** @noinspection CharsetObjectCanBeUsed*/
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "changepassword.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    String data = URLEncoder.encode("id", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8.name()) +
                            "&" + URLEncoder.encode("current_password", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(current, StandardCharsets.UTF_8.name()) +
                            "&" + URLEncoder.encode("new_password", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(newPass, StandardCharsets.UTF_8.name());
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        if (json.getString("status").equals("success")) {
                            Toast.makeText(Settings.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Settings.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(Settings.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Settings.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
