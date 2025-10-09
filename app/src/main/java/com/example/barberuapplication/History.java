package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class History extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    /** @noinspection FieldCanBeLocal*/

    private HistoryAdapter historyAdapter;
    /** @noinspection FieldCanBeLocal*/

    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointmenthistory);

        ImageView retbutton = findViewById(R.id.return_button);
        ImageView homebutton = findViewById(R.id.homeview);

        retbutton.setOnClickListener(v -> finish());

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(History.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

        historyRecyclerView = findViewById(R.id.history_recyclerview);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String customername = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("fullname", "User");

        new FetchHistoryTask(customername).execute();
    }

    /** @noinspection UnusedAssignment*/
    @SuppressLint("StaticFieldLeak")
    private class FetchHistoryTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        FetchHistoryTask(String customername) {
            this.customername = customername;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "get_history.php");
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
                Log.e("FetchHistoryTask", "Error fetching history: " + e.getMessage());
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.startsWith("Error:")) { // Added check for network error prefix
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if ("success".equals(status)) {
                        historyList = new ArrayList<>();
                        JSONArray historyArray = jsonResponse.getJSONArray("history");
                        for (int i = 0; i < historyArray.length(); i++) {
                            JSONObject appointment = historyArray.getJSONObject(i);

                            // IMPORTANT SAFETY CHANGES: Using optString for resilience
                            int id = appointment.getInt("id");
                            String haircutName = appointment.getString("haircutName");
                            String colorName = appointment.optString("colorName", "None"); // Default to "None"
                            String barberName = appointment.optString("barberName", "N/A"); // Default to "N/A"
                            String price = appointment.optString("price", "0.00"); // Safest option for price
                            String dateTime = appointment.getString("dateTime");
                            String statusText = appointment.getString("status");

                            historyList.add(new HistoryItem(
                                    id, haircutName, colorName, barberName, price, dateTime, statusText
                            ));
                        }
                        historyAdapter = new HistoryAdapter(historyList);
                        historyRecyclerView.setAdapter(historyAdapter);
                    } else {
                        historyRecyclerView.setVisibility(View.GONE);
                        Toast.makeText(History.this,
                                "No appointment history yet.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("FetchHistoryTask", "JSON parsing error: " + e.getMessage() + ". Data: " + result); // Log the result data for better debugging
                    Toast.makeText(History.this, "Error parsing history data.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e("FetchHistoryTask", "Server/Network Error: " + (result != null ? result : "Null result"));
                Toast.makeText(History.this, "Failed to connect to server or network error.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
