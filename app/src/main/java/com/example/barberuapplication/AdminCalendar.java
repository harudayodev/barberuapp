package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class AdminCalendar extends AppCompatActivity {

    private LinearLayout scheduleList;
    private String employeeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_calendar);

        scheduleList = findViewById(R.id.schedule_list);
        ImageView returnButton = findViewById(R.id.return_button);
        ImageView homeButton = findViewById(R.id.homeview);

        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());

        // ✅ Get email directly from intent
        Intent intent = getIntent();
        employeeEmail = intent.getStringExtra("email");

        if (employeeEmail == null || employeeEmail.isEmpty()) {
            Toast.makeText(this, "Email not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Fetch schedule on load
        fetchSchedule();
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchSchedule() {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "get_employee_schedule.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String postData = "email=" + employeeEmail;

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return new JSONObject(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @SuppressLint({"InflateParams", "SetTextI18n"})
            @Override
            protected void onPostExecute(JSONObject response) {
                if (response == null) {
                    Toast.makeText(AdminCalendar.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    if (response.getString("status").equals("success")) {
                        JSONObject schedule = response.getJSONObject("schedule");
                        scheduleList.removeAllViews();

                        LayoutInflater inflater = LayoutInflater.from(AdminCalendar.this);
                        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                        for (int i = 0; i < days.length; i++) {
                            String day = days[i];
                            JSONObject dayObj = schedule.optJSONObject(day);
                            View card = inflater.inflate(R.layout.item_schedule_day, null);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 0, 0, 32);
                            card.setLayoutParams(params);

                            CardView cardView = (CardView) card;
                            cardView.setCardBackgroundColor(getResources().getColor(
                                    (i % 2 == 0) ? R.color.card_light_gray : R.color.card_white
                            ));

                            TextView dayName = card.findViewById(R.id.day_name);
                            EditText startTime = card.findViewById(R.id.start_time);
                            EditText endTime = card.findViewById(R.id.end_time);
                            MaterialButton saveBtn = card.findViewById(R.id.save_day_btn);

                            dayName.setText(day);

                            // ✅ Convert fetched 24-hour times to 12-hour format for display
                            String startRaw = dayObj != null ? dayObj.optString("start_time", "none") : "none";
                            String endRaw = dayObj != null ? dayObj.optString("end_time", "none") : "none";

                            if (!startRaw.equals("none") && startRaw.contains(":"))
                                startTime.setText(convert24To12(startRaw));
                            else
                                startTime.setText("none");

                            if (!endRaw.equals("none") && endRaw.contains(":"))
                                endTime.setText(convert24To12(endRaw));
                            else
                                endTime.setText("none");

                            startTime.setOnClickListener(v -> showTimePicker(startTime));
                            endTime.setOnClickListener(v -> showTimePicker(endTime));

                            saveBtn.setOnClickListener(v -> {
                                String start = startTime.getText().toString().trim();
                                String end = endTime.getText().toString().trim();

                                if (start.equals("none") || end.equals("none") || start.isEmpty() || end.isEmpty()) {
                                    Toast.makeText(AdminCalendar.this, "Please select both times.", Toast.LENGTH_SHORT).show();
                                } else {
                                    new AlertDialog.Builder(AdminCalendar.this)
                                            .setTitle("Confirm Save")
                                            .setMessage("Save schedule for " + day + "?\nStart: " + start + "\nEnd: " + end)
                                            .setPositiveButton("Save", (dialog, which) ->
                                                    saveSchedule(day, convertTo24Hour(start), convertTo24Hour(end)))
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                            });

                            scheduleList.addView(card);
                        }
                    } else {
                        Toast.makeText(AdminCalendar.this, response.optString("message", "No data found"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AdminCalendar.this, "JSON parsing error.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void showTimePicker(EditText editText) {
        int hour = 8, minute = 0;
        String currentText = editText.getText().toString();

        if (!currentText.equals("none") && currentText.contains(":")) {
            try {
                String[] parts = currentText.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1].substring(0, 2));
            } catch (Exception ignored) {}
        }

        TimePickerDialog picker = new TimePickerDialog(
                this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    if (selectedHour < 8 || selectedHour > 16 || (selectedHour == 16 && selectedMinute > 0)) {
                        Toast.makeText(this, "Please choose a time between 8:00 AM and 4:00 PM.", Toast.LENGTH_SHORT).show();
                        view.postDelayed(() -> showTimePicker(editText), 300);
                        return;
                    }
                    editText.setText(formatTo12Hour(selectedHour, selectedMinute));
                },
                hour, minute, false
        );
        picker.show();
    }

    // ✅ Convert 24-hour time (e.g., "13:30") → "1:30 PM"
    private String convert24To12(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String amPm = (hour >= 12) ? "PM" : "AM";
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm);
        } catch (Exception e) {
            return "none";
        }
    }

    private String formatTo12Hour(int hour, int minute) {
        String amPm = (hour >= 12) ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm);
    }

    private String convertTo24Hour(String time12) {
        try {
            time12 = time12.trim().toUpperCase();
            String amPm = time12.endsWith("PM") ? "PM" : "AM";
            time12 = time12.replace("AM", "").replace("PM", "").trim();

            String[] parts = time12.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (amPm.equals("PM") && hour != 12) hour += 12;
            if (amPm.equals("AM") && hour == 12) hour = 0;

            return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        } catch (Exception e) {
            return "08:00";
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void saveSchedule(String day, String start, String end) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "save_employee_availability.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "email=" + employeeEmail +
                            "&day=" + day +
                            "&start_time=" + start +
                            "&end_time=" + end +
                            "&available=true";

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    return new JSONObject(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                if (response != null) {
                    Toast.makeText(AdminCalendar.this, response.optString("message", "Saved"), Toast.LENGTH_SHORT).show();
                    fetchSchedule(); // ✅ Refresh UI after saving
                } else {
                    Toast.makeText(AdminCalendar.this, "Error saving schedule.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
