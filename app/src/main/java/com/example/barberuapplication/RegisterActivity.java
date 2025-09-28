package com.example.barberuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText fnameInput, lnameInput, emailInput, passwordInput, confirmpassInput;
    Button registerBtn;
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
        termsCheckbox = findViewById(R.id.terms_checkbox);
        dbHelper = new DbHelper();

        registerBtn.setOnClickListener(v -> {
            String fname = fnameInput.getText().toString().trim();
            String lname = lnameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmpass = confirmpassInput.getText().toString();

            if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmpass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.registerUser(fname, lname, email, password, (status, message, data) -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    if ("success".equalsIgnoreCase(status)) {
                        // Clear fields
                        fnameInput.setText("");
                        lnameInput.setText("");
                        emailInput.setText("");
                        passwordInput.setText("");
                        confirmpassInput.setText("");

                        // Redirect to login
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            });
        });

        signInText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }
}
