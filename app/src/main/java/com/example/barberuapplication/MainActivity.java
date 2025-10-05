package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

public class MainActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            finishAffinity();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        EditText emailInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginBtn = findViewById(R.id.login_btn);
        Button registerBtn = findViewById(R.id.register_btn);
        ImageView showPasswordIcon = findViewById(R.id.showpassword);
        TextView resetPass = findViewById(R.id.resetpass);

        DbHelper dbHelper = new DbHelper();

        showPasswordIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPasswordIcon.setImageResource(R.drawable.showpass);
                isPasswordVisible = false;
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPasswordIcon.setImageResource(R.drawable.hidepass);
                isPasswordVisible = true;
            }
            passwordInput.setSelection(passwordInput.getText().length());
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.loginUser(email, password, (status, message, data) -> {
                if ("success".equalsIgnoreCase(status)) {
                    int userId = data.optInt("id", 0);
                    String fname = data.optString("firstname", "");
                    String lname = data.optString("lastname", "");
                    String fullname = fname + " " + lname;
                    String role = data.optString("role", "user");

                    String employeeId = data.optString("employee_id", null);
                    String shopId = data.optString("shop_id", null);
                    String shopName = data.optString("shop_name", null);

                    // ⭐ CRITICAL FIX: Changed key from "id" to "userID"
                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("userID", userId) // <-- This now matches RatingAdapter.java
                            .putString("fullname", fullname)
                            .putString("role", role)
                            .putString("employee_id", employeeId)
                            .putString("shop_id", shopId)
                            .putString("shop_name", shopName)
                            .apply();

                    Intent intent;
                    if ("admin".equalsIgnoreCase(role)) {
                        intent = new Intent(MainActivity.this, HomepageAdmin.class);
                        intent.putExtra("employee_id", employeeId);
                        intent.putExtra("shop_id", shopId);
                        intent.putExtra("shop_name", shopName);
                    } else {
                        intent = new Intent(MainActivity.this, HomepageActivity.class);
                    }
                    intent.putExtra("fullname", fullname);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        registerBtn.setOnClickListener(view -> {
            Intent signupIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(signupIntent);
        });

        if (resetPass != null) {
            resetPass.setOnClickListener(v -> {
                Intent resetIntent = new Intent(MainActivity.this, ResetPassActivity.class);
                startActivity(resetIntent);
            });
        }
    }
}