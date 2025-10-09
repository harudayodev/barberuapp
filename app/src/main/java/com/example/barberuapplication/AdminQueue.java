package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class AdminQueue extends AppCompatActivity implements AdminQueueAdapter.OnActionButtonClickListener {

    private static final String BASE_URL = Config.BASE_URL;

    /** @noinspection FieldCanBeLocal*/
    private RecyclerView recyclerView;
    private AdminQueueAdapter adapter;
    private List<QueueItem> queueList;

    /** @noinspection FieldCanBeLocal*/
    private int adminID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminqueue);

        // Get admin ID from SharedPreferences
        adminID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("employee_id", -1);

        ImageView returnButton = findViewById(R.id.return_button);
        ImageView homeButton = findViewById(R.id.homeview);

        returnButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminQueue.this, HomepageAdmin.class));
            finish();
        });

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminQueue.this, HomepageAdmin.class));
            finish();
        });

        if (adminID == -1) {
            Toast.makeText(this, "Admin ID not found. Please log in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.admin_queue_recyclerview);
        queueList = new ArrayList<>();
        adapter = new AdminQueueAdapter(queueList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new FetchQueueTask().execute(String.valueOf(adminID));
    }

    @Override
    public void onAcceptClick(int queueID, int position) {
        new UpdateQueueStatusTask(queueID, "accept", position).execute();
    }

    @Override
    public void onDeclineClick(int queueID, int position) {
        new UpdateQueueStatusTask(queueID, "decline", position).execute();
    }

    @SuppressLint("NewApi")
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
            try {
                if (reader != null) reader.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchQueueTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Map<String, String> postData = new HashMap<>();
            postData.put("employeeID", params[0]);
            return fetchData(BASE_URL + "get_admin_queue.php", postData);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if ("success".equals(jsonResponse.getString("status"))) {
                        JSONArray queueArray = jsonResponse.getJSONArray("queue");
                        queueList.clear();
                        for (int i = 0; i < queueArray.length(); i++) {
                            JSONObject item = queueArray.getJSONObject(i);
                            queueList.add(new QueueItem(
                                    item.getInt("QueueID"),
                                    item.getString("name"),
                                    item.getString("barber"),
                                    item.getString("date_time"),
                                    item.getString("Haircut_Name"),
                                    item.optString("Color_Name", ""),
                                    item.optString("Shave_Name", "")
                            ));
                        }
                        adapter.notifyDataSetChanged();

                        if (queueList.isEmpty()) {
                            Toast.makeText(AdminQueue.this, "No appointments in queue.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminQueue.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("FetchQueueTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(AdminQueue.this, "Error processing queue data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminQueue.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AdminQueue.this, HomepageAdmin.class));
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateQueueStatusTask extends AsyncTask<Void, Void, String> {
        private final int queueID;
        private final String action;
        private final int position;

        UpdateQueueStatusTask(int queueID, String action, int position) {
            this.queueID = queueID;
            this.action = action;
            this.position = position;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Map<String, String> postData = new HashMap<>();
            postData.put("queueID", String.valueOf(queueID));
            postData.put("action", action);
            return fetchData(BASE_URL + "update_queue_status.php", postData);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    Toast.makeText(AdminQueue.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    if ("success".equals(jsonResponse.getString("status"))) {
                        adapter.removeItem(position);
                    }
                } catch (JSONException e) {
                    Log.e("UpdateQueueStatusTask", "JSON parsing error: " + e.getMessage());
                    Toast.makeText(AdminQueue.this, "Error updating status.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdminQueue.this, "Server error during status update.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
