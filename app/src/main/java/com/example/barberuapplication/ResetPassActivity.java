package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ResetPassActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button sendEmailButton;
    private ImageView backButton;
    private ConstraintLayout modalOverlay;
    private Button modalDismissButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resetpass);

        // Get references to UI elements
        emailInput = findViewById(R.id.email_input);
        sendEmailButton = findViewById(R.id.send_email_button);
        backButton = findViewById(R.id.back_button);
        modalOverlay = findViewById(R.id.modal_overlay);
        modalDismissButton = findViewById(R.id.modal_dismiss_button);

        // Set up click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity and go back
            }
        });

        // Set up click listener for the send email button
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ResetPassActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                } else {
                    // Simulate a successful email sent and show the custom alert
                    modalOverlay.setVisibility(View.VISIBLE);
                }
            }
        });

        // Set up click listener for the modal's dismiss button
        modalDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modalOverlay.setVisibility(View.GONE); // Hide the custom alert
            }
        });
    }
}