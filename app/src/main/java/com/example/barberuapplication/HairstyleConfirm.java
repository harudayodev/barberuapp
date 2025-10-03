package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HairstyleConfirm extends AppCompatActivity {

    private static final String BASE_URL = Config.BASE_URL;
    private ImageView returnButton;
    private TextView customerNameInput;
    private Spinner haircutNameInput, haircutColorInput, branchInput, barberInput;
    private List<String> haircutNames = new ArrayList<>();
    private List<String> haircutColors = new ArrayList<>();
    private TextView datetimePicker;
    private Button confirmButton;
    private Map<String, Integer> barbershopMap = new HashMap<>();
    private List<String> barbershopNames = new ArrayList<>();
    private List<String> barberNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hairstyleconfirm);

        returnButton = findViewById(R.id.return_button_confirm);
        customerNameInput = findViewById(R.id.customer_name_input);
        haircutNameInput = findViewById(R.id.haircut_name_input);
        haircutColorInput = findViewById(R.id.haircut_color_input);
        branchInput = findViewById(R.id.branch_input);
        barberInput = findViewById(R.id.barber_input);
        datetimePicker = findViewById(R.id.datetime_picker);
        confirmButton = findViewById(R.id.confirm_button);

        String fullname = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");
        customerNameInput.setText(fullname);

        new FetchBarbershopsTask().execute();

        datetimePicker.setOnClickListener(v -> showDateTimePickerDialog());

        branchInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBranchName = barbershopNames.get(position);
                Integer shopID = barbershopMap.get(selectedBranchName);
                if (shopID != null) {
                    new FetchHaircutsTask().execute(String.valueOf(shopID));
                    new FetchBarbersTask().execute(String.valueOf(shopID));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        returnButton.setOnClickListener(v -> finish());
        confirmButton.setOnClickListener(v -> checkAndSaveAppointment());
    }

    private void showDateTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) ->
                        showTimePickerDialog(selectedYear, selectedMonth, selectedDay),
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        datePickerDialog.show();
    }

    private void showTimePickerDialog(int year, int month, int day) {
        // Default to 8:00 AM
        int hour = 8;
        int minute = 0;

        @SuppressLint("SetTextI18n") TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Check if within 8AM - 4PM
                    if (selectedHour < 8 || selectedHour > 16 || (selectedHour == 16 && selectedMinute > 0)) {
                        Toast.makeText(this, "Please select a time between 8:00 AM and 4:00 PM", Toast.LENGTH_SHORT).show();
                        showTimePickerDialog(year, month, day); // reopen picker
                        return;
                    }

                    Calendar selectedDateTime = Calendar.getInstance();
                    selectedDateTime.set(year, month, day, selectedHour, selectedMinute);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    datetimePicker.setText(dateFormatter.format(selectedDateTime.getTime()) + " " +
                            timeFormatter.format(selectedDateTime.getTime()));
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void checkAndSaveAppointment() {
        String customername = customerNameInput.getText().toString();
        String branch = branchInput.getSelectedItem() != null ? branchInput.getSelectedItem().toString() : "";
        String haircut = haircutNameInput.getSelectedItem() != null ? haircutNameInput.getSelectedItem().toString() : "";
        String color = haircutColorInput.getSelectedItem() != null ? haircutColorInput.getSelectedItem().toString() : "";
        String barber = barberInput.getSelectedItem() != null ? barberInput.getSelectedItem().toString() : "";
        String dateTime = datetimePicker.getText().toString();

        if (branch.isEmpty() || haircut.isEmpty() || barber.isEmpty() || dateTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new CheckActiveAppointmentTask(customername, branch, haircut, color, barber, dateTime).execute();
    }

    private void showConfirmDialog(final String customername, final String branch, final String haircut,
                                   final String color, final String barber, final String dateTime) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Appointment?")
                .setMessage("Are you sure you want to confirm this appointment?")
                .setPositiveButton("Yes", (dialog, which) ->
                        saveAppointment(customername, haircut, color, dateTime, branch, barber))
                .setNegativeButton("No", null)
                .show();
    }

    private void saveAppointment(String customername, String haircut, String color, String dateTime,
                                 String branch, String barber) {
        String[] dateTimeParts = dateTime.split(" ");
        String date = dateTimeParts[0];
        String timeslot = dateTimeParts[1];

        new SaveAppointmentTask(customername, haircut, color, date, timeslot, branch, barber).execute();
    }

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
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : postData.entrySet()) {
                    if (first) first = false;
                    else result.append("&");
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
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

    // ---------------- ASYNC TASKS ---------------- //

    @SuppressLint("StaticFieldLeak")
    private class FetchBarbershopsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return fetchData(BASE_URL + "get_barbershops.php", null);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    barbershopNames.clear();
                    barbershopMap.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject shop = jsonArray.getJSONObject(i);
                        String name = shop.getString("name");
                        int shopID = shop.getInt("shopID");
                        barbershopNames.add(name);
                        barbershopMap.put(name, shopID);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(HairstyleConfirm.this,
                            android.R.layout.simple_spinner_item, barbershopNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    branchInput.setAdapter(adapter);
                } catch (JSONException e) {
                    Log.e("FetchBarbershopsTask", "JSON parsing error: " + e.getMessage());
                }
            } else {
                Toast.makeText(HairstyleConfirm.this, "Failed to fetch barbershops.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchHaircutsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String shopID = params[0];
            Map<String, String> postData = new HashMap<>();
            postData.put("shopID", shopID);
            return fetchData(BASE_URL + "get_haircuts.php", postData);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if ("fail".equals(jsonResponse.optString("status"))) {
                        Toast.makeText(HairstyleConfirm.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    haircutNames.clear();
                    haircutColors.clear();
                    JSONArray namesArray = jsonResponse.optJSONArray("haircut_names");
                    JSONArray colorsArray = jsonResponse.optJSONArray("color_names");

                    if (namesArray != null) {
                        for (int i = 0; i < namesArray.length(); i++) {
                            haircutNames.add(namesArray.getString(i));
                        }
                    }

                    if (colorsArray != null) {
                        for (int i = 0; i < colorsArray.length(); i++) {
                            haircutColors.add(colorsArray.getString(i));
                        }
                    }

                    ArrayAdapter<String> haircutAdapter = new ArrayAdapter<>(HairstyleConfirm.this,
                            android.R.layout.simple_spinner_item, haircutNames);
                    haircutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    haircutNameInput.setAdapter(haircutAdapter);

                    ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(HairstyleConfirm.this,
                            android.R.layout.simple_spinner_item, haircutColors);
                    colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    haircutColorInput.setAdapter(colorAdapter);

                } catch (JSONException e) {
                    Log.e("FetchHaircutsTask", "JSON parsing error: " + e.getMessage());
                }
            } else {
                Toast.makeText(HairstyleConfirm.this, "Failed to fetch haircuts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchBarbersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String shopID = params[0];
            Map<String, String> postData = new HashMap<>();
            postData.put("shopID", shopID);
            return fetchData(BASE_URL + "get_barbers.php", postData);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    barberNames.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject barber = jsonArray.getJSONObject(i);
                        barberNames.add(barber.getString("FirstName") + " " + barber.getString("LastName"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(HairstyleConfirm.this,
                            android.R.layout.simple_spinner_item, barberNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    barberInput.setAdapter(adapter);
                } catch (JSONException e) {
                    Log.e("FetchBarbersTask", "JSON parsing error: " + e.getMessage());
                }
            } else {
                Toast.makeText(HairstyleConfirm.this, "Failed to fetch barbers.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckActiveAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername, branch, haircut, color, barber, dateTime;

        CheckActiveAppointmentTask(String customername, String branch, String haircut,
                                   String color, String barber, String dateTime) {
            this.customername = customername;
            this.branch = branch;
            this.haircut = haircut;
            this.color = color;
            this.barber = barber;
            this.dateTime = dateTime;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Map<String, String> postData = new HashMap<>();
            postData.put("customername", customername);
            postData.put("branch", branch);
            postData.put("haircut", haircut);
            postData.put("color", color);
            postData.put("barber", barber);
            postData.put("dateTime", dateTime);
            return fetchData(BASE_URL + "check_active_appointment.php", postData);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    if ("exists".equals(status)) {
                        new AlertDialog.Builder(HairstyleConfirm.this)
                                .setTitle("\uD83D\uDEA8 Appointment Exists \uD83D\uDEA8")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    } else if ("not_exists".equals(status)) {
                        HairstyleConfirm.this.showConfirmDialog(customername, branch, haircut, color, barber, dateTime);
                    } else {
                        Toast.makeText(HairstyleConfirm.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("CheckTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(HairstyleConfirm.this, "Error checking for active appointment.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HairstyleConfirm.this, "Server error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveAppointmentTask extends AsyncTask<Void, Void, String> {
        private final String customername, haircut, color, date, timeslot, branch, barber;

        SaveAppointmentTask(String customername, String haircut, String color,
                            String date, String timeslot, String branch, String barber) {
            this.customername = customername;
            this.haircut = haircut;
            this.color = color;
            this.date = date;
            this.timeslot = timeslot;
            this.branch = branch;
            this.barber = barber;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Map<String, String> postData = new HashMap<>();
            postData.put("customername", customername);
            postData.put("haircut", haircut);
            postData.put("color", color);
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
                        // Go back to HomepageActivity
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
