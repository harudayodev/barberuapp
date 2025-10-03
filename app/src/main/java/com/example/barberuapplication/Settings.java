package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Settings extends AppCompatActivity {

    private TextView usernameText;
    private int userId;

    @SuppressLint("MissingInflatedId")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        usernameText = findViewById(R.id.user_name);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("id", 0);

        // Load from SharedPreferences only
        String fullname = prefs.getString("fullname", "User");
        usernameText.setText(fullname);

        ImageView returnButton = findViewById(R.id.returnb);
        returnButton.setOnClickListener(v -> finish());

        ImageView editProfile = findViewById(R.id.editprofile);
        editProfile.setOnClickListener(v -> showEditDialog());

        findViewById(R.id.change_password_container).setOnClickListener(v -> showChangePasswordDialog());
    }

    protected void onResume() {
        super.onResume();
        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        usernameText.setText(fullname);
    }

    private void showEditDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_change_username, null);

        EditText firstNameInput = dialogView.findViewById(R.id.edit_firstname);
        EditText lastNameInput = dialogView.findViewById(R.id.edit_lastname);

        // Pre-fill with current name if available
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
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "editprofile.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    // Use BufferedWriter and URLEncoder for robust data handling
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

                    String data = URLEncoder.encode("id", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8.name()) + "&" +
                            URLEncoder.encode("firstname", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(firstname, StandardCharsets.UTF_8.name()) + "&" +
                            URLEncoder.encode("lastname", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(lastname, StandardCharsets.UTF_8.name());

                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

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

                            // Save updated name
                            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("fullname", fullname)
                                    .apply();

                            Toast.makeText(Settings.this, "Profile updated", Toast.LENGTH_SHORT).show();
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

    private void showChangePasswordDialog() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

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

                    String data = URLEncoder.encode("id", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(String.valueOf(userId), StandardCharsets.UTF_8.name()) + "&" +
                            URLEncoder.encode("current_password", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(current, StandardCharsets.UTF_8.name()) + "&" +
                            URLEncoder.encode("new_password", StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(newPass, StandardCharsets.UTF_8.name());

                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
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