package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText; // Import EditText

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.text.Editable; // Import Editable
import android.text.TextWatcher; // Import TextWatcher
import android.view.inputmethod.InputMethodManager;
import android.content.Context; // Import Context

public class OTPActivity extends AppCompatActivity {

    Button return_btn;
    Button resendButton;

    // Add the EditText fields
    EditText otpInput1, otpInput2, otpInput3, otpInput4;

    private static final long COUNTDOWN_TIME = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpactivity);

        return_btn = findViewById(R.id.return_btn);
        resendButton = findViewById(R.id.resend_link);

        otpInput1 = findViewById(R.id.otp_input_1);
        otpInput2 = findViewById(R.id.otp_input_2);
        otpInput3 = findViewById(R.id.otp_input_3);
        otpInput4 = findViewById(R.id.otp_input_4);

        return_btn.setOnClickListener(v -> {
            new AlertDialog.Builder(OTPActivity.this)
                    .setTitle("Exit OTP")
                    .setMessage("Are you sure you want to go back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(OTPActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });

        resendButton.setOnClickListener(v -> {
            startCountdown();
        });

        startCountdown();

        otpInput1.addTextChangedListener(new OtpTextWatcher(otpInput2, null));
        otpInput2.addTextChangedListener(new OtpTextWatcher(otpInput3, otpInput1));
        otpInput3.addTextChangedListener(new OtpTextWatcher(otpInput4, otpInput2));
        otpInput4.addTextChangedListener(new OtpTextWatcher(null, otpInput3));
    }

    private void startCountdown() {
        resendButton.setEnabled(false);
        resendButton.setAlpha(0.5f);

        new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                resendButton.setText("RESEND in " + (millisUntilFinished / 1000) + "s");
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                resendButton.setEnabled(true);
                resendButton.setAlpha(1.0f);
                resendButton.setText("RESEND");
            }
        }.start();
    }
    private class OtpTextWatcher implements TextWatcher {
        private final EditText nextField;
        private final EditText previousField;

        OtpTextWatcher(EditText nextField, EditText previousField) {
            this.nextField = nextField;
            this.previousField = previousField;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) {
                if (nextField != null) {
                    nextField.requestFocus();
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(otpInput4.getWindowToken(), 0);
                }
            } else if (s.length() == 0) {
                if (previousField != null) {
                    previousField.requestFocus();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
