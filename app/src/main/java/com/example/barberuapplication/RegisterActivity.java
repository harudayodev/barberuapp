package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText fnameInput, lnameInput, emailInput, passwordInput, confirmpassInput;
    Button registerBtn, returnBtn;
    TextView signInText;
    CheckBox termsCheckbox;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fnameInput = findViewById(R.id.fname_input);
        lnameInput = findViewById(R.id.lname_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmpassInput = findViewById(R.id.conpassword_input);
        registerBtn = findViewById(R.id.register_b);
        signInText = findViewById(R.id.signInText);
        returnBtn = findViewById(R.id.return_btn);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        dbHelper = new DbHelper();

        registerBtn.setOnClickListener(v -> handleRegister());

        signInText.setOnClickListener(v -> finish());

        returnBtn.setOnClickListener(v -> handleReturn());
    }

    private void handleRegister() {
        String fname = fnameInput.getText().toString().trim();
        String lname = lnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmpass = confirmpassInput.getText().toString();

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpass.isEmpty()) {
            showToast("All fields required");
            return;
        }

        if (!password.equals(confirmpass)) {
            showToast("Passwords do not match");
            return;
        }

        if (password.length() < 8) {
            showToast("Password must be at least 8 characters long");
            return;
        }

        if (!termsCheckbox.isChecked()) {
            showToast("Please agree to the Terms & Conditions");
            return;
        }

        dbHelper.registerUser(fname, lname, email, password, (status, message, data) -> runOnUiThread(() -> {
            if ("success".equalsIgnoreCase(status)) {
                showSuccessDialog(fname + " " + lname);
            } else {
                showToast(message);
            }
        }));
    }

    @SuppressLint("SetTextI18n")
    private void showSuccessDialog(String fullname) {
        // Inflate compact custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register_success, null);
        TextView messageText = dialogView.findViewById(R.id.message_text);
        messageText.setText("Welcome, " + fullname + "!\nYour account has been created.");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Remove default white padding and apply fade animation
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.FadeDialogAnimation;
        }

        dialog.show();

        // Apply bounce animation
        dialogView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));

        // Auto-close after 2.5 seconds and redirect to login
        dialogView.postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3500);
    }

    private void handleReturn() {
        if (hasInput()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("Are you sure you want to return?\nAll input will be lost.")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            finish();
        }
    }

    private boolean hasInput() {
        return !fnameInput.getText().toString().trim().isEmpty() ||
                !lnameInput.getText().toString().trim().isEmpty() ||
                !emailInput.getText().toString().trim().isEmpty() ||
                !passwordInput.getText().toString().trim().isEmpty() ||
                !confirmpassInput.getText().toString().trim().isEmpty();
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
