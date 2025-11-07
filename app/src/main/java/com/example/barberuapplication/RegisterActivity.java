package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

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

        TextInputLayout fnameLayout = findViewById(R.id.fname_layout);
        TextInputLayout lnameLayout = findViewById(R.id.lname_layout);
        TextInputLayout emailLayout = findViewById(R.id.email_layout);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        TextInputLayout conpasswordLayout = findViewById(R.id.conpassword_layout);

        fnameLayout.setExpandedHintEnabled(true);
        lnameLayout.setExpandedHintEnabled(true);
        emailLayout.setExpandedHintEnabled(true);
        passwordLayout.setExpandedHintEnabled(true);
        conpasswordLayout.setExpandedHintEnabled(true);

        fnameLayout.setHintTextAppearance(R.style.EnlargedHintStyle);
        lnameLayout.setHintTextAppearance(R.style.EnlargedHintStyle);
        emailLayout.setHintTextAppearance(R.style.EnlargedHintStyle);
        passwordLayout.setHintTextAppearance(R.style.EnlargedHintStyle);
        conpasswordLayout.setHintTextAppearance(R.style.EnlargedHintStyle);



    }

    private void handleRegister() {
        if (!validateInputs()) return;

        String fname = fnameInput.getText().toString().trim();
        String lname = lnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        dbHelper.registerUser(fname, lname, email, password, (status, message, data) -> runOnUiThread(() -> {
            if ("success".equalsIgnoreCase(status)) {
                showSuccessDialog(fname + " " + lname);
            } else {
                showToast(message);
            }
        }));
    }

    private boolean validateInputs() {
        String fname = fnameInput.getText().toString().trim();
        String lname = lnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmpassInput.getText().toString();

        if (fname.isEmpty()) {
            showToast("First name is required.");
            return false;
        }
        if (lname.isEmpty()) {
            showToast("Last name is required.");
            return false;
        }
        if (email.isEmpty()) {
            showToast("Email address is required.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            showToast("Only @gmail.com email addresses are accepted.");
            return false;
        }
        if (password.isEmpty()) {
            showToast("Password is required.");
            return false;
        }
        if (password.length() < 8) {
            showToast("Password must be at least 8 characters long.");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            showToast("Password must contain at least one uppercase letter.");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            showToast("Password must contain at least one lowercase letter.");
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            showToast("Password must contain at least one number.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match.");
            return false;
        }
        if (!termsCheckbox.isChecked()) {
            showToast("Please agree to the Terms & Conditions.");
            return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void showSuccessDialog(String fullname) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register_success, null);
        TextView messageText = dialogView.findViewById(R.id.message_text);
        messageText.setText("Welcome, " + fullname + "!\nYour account has been created.");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.FadeDialogAnimation;
        }

        dialog.show();
        dialogView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));

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
        return !fnameInput.getText().toString().trim().isEmpty()
                || !lnameInput.getText().toString().trim().isEmpty()
                || !emailInput.getText().toString().trim().isEmpty()
                || !passwordInput.getText().toString().trim().isEmpty()
                || !confirmpassInput.getText().toString().trim().isEmpty();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
