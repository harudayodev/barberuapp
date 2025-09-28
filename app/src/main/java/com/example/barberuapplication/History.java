package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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

public class History extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointmenthistory);

        ImageView retbutton = findViewById(R.id.return_button);
        ImageView homebutton = findViewById(R.id.homeview);

        retbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

    @SuppressLint("StaticFieldLeak")
    private class FetchHistoryTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        FetchHistoryTask(String customername) {
            this.customername = customername;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.100.10/barberuapp/get_history.php");
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
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if ("success".equals(status)) {
                        historyList = new ArrayList<>();
                        JSONArray historyArray = jsonResponse.getJSONArray("history");
                        for (int i = 0; i < historyArray.length(); i++) {
                            JSONObject appointment = historyArray.getJSONObject(i);
                            String queueNumber = appointment.getString("queueNumber");
                            String customerName = appointment.getString("customerName");
                            String serviceName = appointment.getString("serviceName");
                            String barberName = appointment.getString("barberName");
                            String price = appointment.getString("price");
                            String date = appointment.getString("date");
                            String timeslot = appointment.getString("timeslot");
                            String statusText = appointment.getString("Status");

                            historyList.add(new HistoryItem(queueNumber, customerName, serviceName, barberName, price, date, timeslot, statusText));
                        }
                        historyAdapter = new HistoryAdapter(historyList);
                        historyRecyclerView.setAdapter(historyAdapter);
                    } else {
                        Toast.makeText(History.this, "No past appointments found.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("FetchHistoryTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(History.this, "Error parsing history data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(History.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}