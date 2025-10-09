package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HairstyleConfirm extends AppCompatActivity {

    private static final String BASE_URL = Config.BASE_URL;

    /** @noinspection FieldCanBeLocal*/
    private ImageView returnButton;
    private TextView customerNameInput, haircutNameInput, haircutColorInput, shaveInput,
            branchInput, barberInput, datetimePicker;

    /** @noinspection FieldCanBeLocal*/
    private Button confirmButton;
    private String[] availableDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hairstyleconfirm);

        // Initialize views
        returnButton = findViewById(R.id.return_button_confirm);
        customerNameInput = findViewById(R.id.customer_name_input);
        haircutNameInput = findViewById(R.id.haircut_name_input);
        haircutColorInput = findViewById(R.id.haircut_color_input);
        shaveInput = findViewById(R.id.shave_input);
        branchInput = findViewById(R.id.branch_input);
        barberInput = findViewById(R.id.barber_input);
        datetimePicker = findViewById(R.id.datetime_picker);
        confirmButton = findViewById(R.id.confirm_button);

        // Get data from intent
        Intent intent = getIntent();
        String haircutName = intent.getStringExtra("selectedHaircutName");
        String shopName = intent.getStringExtra("shopName");
        String barberName = intent.getStringExtra("barberName");
        String availabilityDays = intent.getStringExtra("availabilityDays");

        if (availabilityDays != null) {
            availableDays = availabilityDays.split(",\\s*");
        }

        if (haircutName != null) haircutNameInput.setText(haircutName);
        if (shopName != null) branchInput.setText(shopName);
        if (barberName != null) barberInput.setText(barberName);

        // Set customer name
        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        customerNameInput.setText(fullname);

        datetimePicker.setOnClickListener(v -> showCustomDatePickerDialog());
        returnButton.setOnClickListener(v -> finish());
        confirmButton.setOnClickListener(v -> checkAndSaveAppointment());
    }

    @SuppressLint("SetTextI18n")
    private void showCustomDatePickerDialog() {
        if (availableDays == null || availableDays.length == 0) {
            Toast.makeText(this, "No schedule found for this barber.", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_date_picker, null);
        CalendarView calendarView = dialogView.findViewById(R.id.customCalendarView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Select Date")
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();

        // Restrict to today and onward
        Calendar today = Calendar.getInstance();
        calendarView.setMinDate(today.getTimeInMillis());

        // Handle user selecting a day
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            String dayName = new SimpleDateFormat("EEEE", Locale.US).format(selected.getTime());

            if (isAvailableDay(dayName)) {
                // ✅ Flash the view briefly in green to show valid selection
                view.setBackgroundColor(Color.parseColor("#CCFFCC"));
                view.postDelayed(() -> view.setBackgroundColor(Color.TRANSPARENT), 200);

                dialog.dismiss();
                showTimePickerDialog(year, month, dayOfMonth);
            } else {
                // ❌ Show unavailable message
                Toast.makeText(this, "❌ Barber not available on said day", Toast.LENGTH_SHORT).show();

                // Subtle red flash for feedback
                view.setBackgroundColor(Color.parseColor("#FFCCCC"));
                view.postDelayed(() -> view.setBackgroundColor(Color.TRANSPARENT), 300);
            }
        });

        dialog.show();
    }




    private boolean isAvailableDay(String dayName) {
        if (availableDays == null) return false;
        for (String d : availableDays) {
            if (d.equalsIgnoreCase(dayName)) return true;
        }
        return false;
    }

    private Calendar findNextAvailableDate(Calendar fromDate) {
        Calendar nextDate = (Calendar) fromDate.clone();
        do {
            nextDate.add(Calendar.DAY_OF_MONTH, 1);
        } while (!isAvailableDay(new SimpleDateFormat("EEEE", Locale.US).format(nextDate.getTime())));
        return nextDate;
    }

    /**
     * Time Picker
     */
    private void showTimePickerDialog(int year, int month, int day) {
        final Calendar now = Calendar.getInstance();
        final Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);

        int hour = 8, minute = 0;
        if (isToday(selectedDate)) {
            hour = now.get(Calendar.HOUR_OF_DAY);
            minute = now.get(Calendar.MINUTE) + 1;
            if (hour < 8) hour = 8;
            if (hour > 16) hour = 16;
            if (hour == 16) minute = 0;
        }

        @SuppressLint("SetTextI18n")
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    if (selectedHour < 8 || selectedHour > 16 || (selectedHour == 16 && selectedMinute > 0)) {
                        Toast.makeText(this, "Please select a time between 8:00 AM and 4:00 PM", Toast.LENGTH_SHORT).show();
                        showTimePickerDialog(year, month, day);
                        return;
                    }

                    Calendar selectedDateTime = Calendar.getInstance();
                    selectedDateTime.set(year, month, day, selectedHour, selectedMinute);

                    if (isToday(selectedDate) && selectedDateTime.before(now)) {
                        Toast.makeText(this, "You cannot select a past time today", Toast.LENGTH_SHORT).show();
                        showTimePickerDialog(year, month, day);
                        return;
                    }

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    datetimePicker.setText(dateFormatter.format(selectedDateTime.getTime()) + " " +
                            timeFormatter.format(selectedDateTime.getTime()));
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private boolean isToday(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private void checkAndSaveAppointment() {
        String customername = customerNameInput.getText().toString();
        String branch = branchInput.getText().toString();
        String haircut = haircutNameInput.getText().toString();
        String color = haircutColorInput.getText().toString();
        String shave = shaveInput.getText().toString();
        String barber = barberInput.getText().toString();
        String dateTime = datetimePicker.getText().toString();

        if (branch.isEmpty() || haircut.isEmpty() || barber.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showConfirmDialog(customername, branch, haircut, color, shave, barber, dateTime);
    }

    private void showConfirmDialog(final String customername, final String branch, final String haircut,
                                   final String color, final String shave, final String barber, final String dateTime) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Appointment?")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes", (dialog, which) ->
                        saveAppointment(customername, haircut, color, shave, dateTime, branch, barber))
                .setNegativeButton("No", null)
                .show();
    }

    private void saveAppointment(String customername, String haircut, String color, String shave,
                                 String dateTime, String branch, String barber) {
        String[] dateTimeParts = dateTime.split(" ");
        String date = dateTimeParts[0];
        String timeslot = dateTimeParts[1];
        new SaveAppointmentTask(customername, haircut, color, shave, date, timeslot, branch, barber).execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressWarnings("SameParameterValue")
    private String fetchData(String requestUrl, Map<String, String> postData) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(postData == null ? "GET" : "POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);

            if (postData != null && !postData.isEmpty()) {
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : postData.entrySet()) {
                    if (first) first = false;
                    else result.append("&");
                    result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                }
                writer.write(result.toString());
                writer.flush();
                writer.close();
                os.close();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } else {
                Log.e("fetchData", "Server returned: " + responseCode);
            }
        } catch (Exception e) {
            Log.e("fetchData", "Error: " + e.getMessage(), e);
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername, haircut, color, shave, date, timeslot, branch, barber;

        SaveAppointmentTask(String customername, String haircut, String color, String shave,
                            String date, String timeslot, String branch, String barber) {
            this.customername = customername;
            this.haircut = haircut;
            this.color = color;
            this.shave = shave;
            this.date = date;
            this.timeslot = timeslot;
            this.branch = branch;
            this.barber = barber;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            Map<String, String> postData = new HashMap<>();
            postData.put("customername", customername);
            postData.put("haircut", haircut);
            postData.put("color", color);
            postData.put("shave", shave);
            postData.put("date", date);
            postData.put("timeslot", timeslot);
            postData.put("branch", branch);
            postData.put("barber", barber);

            return fetchData(BASE_URL + "save_appointment.php", postData);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");

                    Toast.makeText(HairstyleConfirm.this, message, Toast.LENGTH_LONG).show();

                    if ("success".equals(status)) {
                        Intent intent = new Intent(HairstyleConfirm.this, HomepageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                } catch (JSONException e) {
                    Log.e("SaveAppointmentTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(HairstyleConfirm.this, "Error processing server response.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HairstyleConfirm.this, "Server error during appointment saving.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
