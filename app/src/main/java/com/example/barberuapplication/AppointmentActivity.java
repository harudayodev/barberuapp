package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AppointmentActivity extends AppCompatActivity {

    private TextView customerNameTextView, haircutNameTextView, dateValueTextView, timeValueTextView, branchNameTextView, barberNameTextView, queueStatusText, queueNumberValueTextView, noAppointmentText;
    private View queueInfoCard;
    private Button cancelButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        ImageView returnbutton = findViewById(R.id.return_button);
        ImageView homebutton = findViewById(R.id.homeview);

        queueStatusText = findViewById(R.id.queue_status_text);
        noAppointmentText = findViewById(R.id.no_appointment_text);
        queueInfoCard = findViewById(R.id.queue_info_card);
        cancelButton = findViewById(R.id.cancel_queue_btn);

        queueInfoCard.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        customerNameTextView = findViewById(R.id.customername);
        haircutNameTextView = findViewById(R.id.haircutname);
        dateValueTextView = findViewById(R.id.date_value);
        timeValueTextView = findViewById(R.id.time_of_issue_value);
        branchNameTextView = findViewById(R.id.branchname);
        barberNameTextView = findViewById(R.id.barbername);
        queueNumberValueTextView = findViewById(R.id.queue_number_value);

        String customername = getIntent().getStringExtra("fullname");
        if (customername == null || customername.isEmpty()) {
            customername = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("fullname", "User");
        }

        new FetchAppointmentTask(customername).execute();

        returnbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnview = new Intent(AppointmentActivity.this, HomepageActivity.class);
                startActivity(returnview);
                finish();
            }
        });

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });


        String finalCustomername = customername;
        cancelButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want to cancel your appointment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new CancelAppointmentTask(finalCustomername).execute();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        FetchAppointmentTask(String customername) {
            this.customername = customername;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.100.10/barberuapp/get_appointment.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("customername", "UTF-8") + "=" + URLEncoder.encode(customername, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                inputStream.close();
                result = sb.toString();
                return result;

            } catch (Exception e) {
                Log.e("FetchAppointmentTask", "Error fetching appointment: " + e.getMessage());
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if ("success".equals(status)) {
                        JSONObject appointment = jsonResponse.getJSONObject("appointment");
                        queueStatusText.setText("Your Appointment");
                        queueStatusText.setVisibility(View.VISIBLE);
                        noAppointmentText.setVisibility(View.GONE);
                        queueInfoCard.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);

                        customerNameTextView.setText(customername);
                        haircutNameTextView.setText(appointment.getString("Haircut_Name"));
                        dateValueTextView.setText(appointment.getString("date"));
                        timeValueTextView.setText(appointment.getString("timeslot"));
                        branchNameTextView.setText(appointment.getString("branch"));
                        barberNameTextView.setText(appointment.getString("barbername"));
                        queueNumberValueTextView.setText(appointment.getString("queuenumber"));

                    } else {
                        queueStatusText.setVisibility(View.GONE);
                        noAppointmentText.setVisibility(View.VISIBLE);
                        queueInfoCard.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    // This is where your app was crashing
                    Log.e("FetchAppointmentTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(AppointmentActivity.this, "Error parsing appointment data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AppointmentActivity.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CancelAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        CancelAppointmentTask(String customername) {
            this.customername = customername;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.100.10/barberuapp/cancel_appointment.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String data = URLEncoder.encode("customername", "UTF-8") + "=" + URLEncoder.encode(customername, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                inputStream.close();
                result = sb.toString();
                return result;

            } catch (Exception e) {
                Log.e("CancelAppointmentTask", "Error canceling appointment: " + e.getMessage());
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    Toast.makeText(AppointmentActivity.this, message, Toast.LENGTH_SHORT).show();

                    if ("success".equals(status)) {
                        // Redirect to HomepageActivity and clear the back stack
                        Intent intent = new Intent(AppointmentActivity.this, HomepageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    Log.e("CancelAppointmentTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(AppointmentActivity.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AppointmentActivity.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}