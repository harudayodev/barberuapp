package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.nio.charset.StandardCharsets;

public class AppointmentActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "appointment_channel";

    private TextView customerNameTextView, haircutNameTextView, haircutColorNameTextView, shaveNameTextView,
            dateValueTextView, timeValueTextView, branchNameTextView, barberNameTextView, queueStatusText,
            queueNumberValueTextView, queueTurnMessage, queueMessageTextView, noAppointmentText;
    private View queueInfoCard;
    private Button cancelButton;
    private Handler handler = new Handler();
    private String customername;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
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
        queueTurnMessage = findViewById(R.id.queue_turn_message);
        queueMessageTextView = findViewById(R.id.queue_message_text);

        queueInfoCard.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        customerNameTextView = findViewById(R.id.customername);
        haircutNameTextView = findViewById(R.id.haircutname);
        haircutColorNameTextView = findViewById(R.id.haircutcolorname);
        shaveNameTextView = findViewById(R.id.shavename);
        dateValueTextView = findViewById(R.id.date_value);
        timeValueTextView = findViewById(R.id.time_of_issue_value);
        branchNameTextView = findViewById(R.id.branchname);
        barberNameTextView = findViewById(R.id.barbername);
        queueNumberValueTextView = findViewById(R.id.queue_number_value);

        // Notification channel for Android 8+
        createNotificationChannel();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customername = sharedPreferences.getString("fullname", "User");

        if (customername != null && !customername.isEmpty()) {
            fetchAppointment();
        } else {
            queueStatusText.setVisibility(View.GONE);
            noAppointmentText.setVisibility(View.VISIBLE);
            noAppointmentText.setText("Please log in to view your appointments.");
        }

        returnbutton.setOnClickListener(v -> {
            Intent returnview = new Intent(AppointmentActivity.this, HomepageActivity.class);
            startActivity(returnview);
            finish();
        });

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

        cancelButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel your appointment?")
                .setPositiveButton("Yes", (dialog, which) -> new CancelAppointmentTask(customername).execute())
                .setNegativeButton("No", null)
                .show());

        // Polling every 10 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (customername != null && !customername.isEmpty()) {
                    fetchAppointment();
                }
                handler.postDelayed(this, 10000); // 10 seconds
            }
        }, 10000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for your appointment status");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void fetchAppointment() {
        new FetchAppointmentTask(customername).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        FetchAppointmentTask(String customername) {
            this.customername = customername;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "get_appointment.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("customername", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(customername, StandardCharsets.UTF_8);
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

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.optString("status", "fail");

                    if ("success".equals(status)) {
                        JSONObject appointment = jsonResponse.optJSONObject("appointment");

                        queueStatusText.setText("Your Appointment");
                        queueStatusText.setVisibility(View.VISIBLE);
                        noAppointmentText.setVisibility(View.GONE);
                        queueInfoCard.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);

                        customerNameTextView.setText(customername);
                        haircutNameTextView.setText(appointment.optString("Haircut_Name", "N/A"));
                        haircutColorNameTextView.setText(appointment.optString("Color_Name", "N/A"));
                        shaveNameTextView.setText(appointment.optString("Shave_Name", "N/A"));
                        dateValueTextView.setText(appointment.optString("date", "N/A"));

                        // Format timeslot
                        String time = appointment.optString("timeslot", "N/A");
                        if (!time.equals("N/A")) {
                            String[] parts = time.split(":");
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            String ampm = hour >= 12 ? "PM" : "AM";
                            hour = hour % 12;
                            if (hour == 0) hour = 12;
                            timeValueTextView.setText(hour + ":" + String.format("%02d", minute) + " " + ampm);
                        } else {
                            timeValueTextView.setText("N/A");
                        }

                        branchNameTextView.setText(appointment.optString("branch", "N/A"));
                        barberNameTextView.setText(appointment.optString("barbername", "N/A"));
                        int queueNumber = appointment.optInt("queuenumber", 0);
                        queueNumberValueTextView.setText(String.valueOf(queueNumber));

                        // Update dynamic queue message
                        String currentQueue = appointment.optString("currentqueue", "0");
                        int currentQueueInt = Integer.parseInt(currentQueue);
                        if (queueNumber == currentQueueInt) {
                            queueTurnMessage.setText("It's your turn!");
                            queueMessageTextView.setText("Please proceed to your barber.");

                            // Send notification
                            sendAppointmentNotification("It's your turn!", "Your appointment is now. Please proceed to your barber.");
                        } else if (queueNumber > currentQueueInt) {
                            queueTurnMessage.setText("Please wait your turn.");
                            queueMessageTextView.setText("Current queue number: " + currentQueue);
                        } else {
                            queueTurnMessage.setText("You missed your turn!");
                            queueMessageTextView.setText("Please contact the shop.");
                        }

                    } else {
                        queueStatusText.setVisibility(View.GONE);
                        noAppointmentText.setVisibility(View.VISIBLE);
                        queueInfoCard.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Log.e("FetchAppointmentTask", "JSON parsing error: " + e.getMessage() + "\nRaw response: " + result);
                    Toast.makeText(AppointmentActivity.this, "Error parsing appointment data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AppointmentActivity.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void sendAppointmentNotification(String title, String message) {
        Intent intent = new Intent(this, AppointmentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Custom vibration pattern: wait 0ms, vibrate 500ms, pause 200ms, vibrate 500ms
        long[] vibrationPattern = {0, 500, 200, 500};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(vibrationPattern)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @SuppressLint("StaticFieldLeak")
    private class CancelAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        CancelAppointmentTask(String customername) {
            this.customername = customername;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "cancel_appointment.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("customername", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(customername, StandardCharsets.UTF_8);
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
                    String status = jsonResponse.optString("status", "fail");
                    String message = jsonResponse.optString("message", "Unknown error");

                    Toast.makeText(AppointmentActivity.this, message, Toast.LENGTH_SHORT).show();

                    if ("success".equals(status)) {
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
