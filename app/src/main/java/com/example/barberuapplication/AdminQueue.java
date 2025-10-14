package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar; // ❤️ ADD THIS IMPORT
import android.widget.TextView;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AdminQueue extends AppCompatActivity implements AdminQueueAdapter.OnQueueItemActionListener {

    private RecyclerView recyclerView;
    private TextView noQueueText;
    private AdminQueueAdapter adapter;
    private List<QueueItem> queueItemList;
    private String employeeId;
    private ProgressBar loadingSpinner; // ❤️ ADD THIS VARIABLE
    private final Handler handler = new Handler();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminqueue);

        ImageView returnButton = findViewById(R.id.return_button);
        ImageView homeButton = findViewById(R.id.homeview);
        noQueueText = findViewById(R.id.no_queue_text);
        recyclerView = findViewById(R.id.admin_queue_recyclerview);
        loadingSpinner = findViewById(R.id.loading_spinner); // ❤️ INITIALIZE THE SPINNER

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        queueItemList = new ArrayList<>();
        adapter = new AdminQueueAdapter(queueItemList, this);
        recyclerView.setAdapter(adapter);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int employeeIdInt = prefs.getInt("employee_id", 0);
        employeeId = (employeeIdInt == 0) ? null : String.valueOf(employeeIdInt);

        //noinspection ConstantValue
        if (employeeId != null && !employeeId.isEmpty()) {
            fetchQueueData();
        } else {
            Toast.makeText(this, "Could not verify employee. Please log in again.", Toast.LENGTH_LONG).show();
            noQueueText.setText("Could not load queue. Please log in again.");
            noQueueText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (employeeId != null) {
                    fetchQueueData();
                }
                handler.postDelayed(this, 20000);
            }
        }, 20000);
    }

    private void fetchQueueData() {
        new FetchQueueTask().execute(employeeId);
    }

    @Override
    public void onAcceptClicked(int queueId) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Appointment")
                .setMessage("Are you sure you want to mark this appointment as completed?")
                .setPositiveButton("Yes", (dialog, which) -> new UpdateStatusTask().execute(String.valueOf(queueId), "Done"))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDeclineClicked(int queueId) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Appointment")
                .setMessage("Are you sure you want to decline this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> new UpdateStatusTask().execute(String.valueOf(queueId), "Cancelled"))
                .setNegativeButton("No", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchQueueTask extends AsyncTask<String, Void, String> {
        // No changes needed in this class
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(String... params) {
            String currentEmployeeId = params[0];
            try {
                URL url = new URL(Config.BASE_URL + "get_employee_queue.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String data = URLEncoder.encode("employeeID", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(currentEmployeeId, StandardCharsets.UTF_8);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { result.append(line); }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                Log.e("FetchQueueTask", "Error fetching data: " + e.getMessage());
                return null;
            }
        }
        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(AdminQueue.this, "Failed to connect to server.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                queueItemList.clear();
                if (jsonObject.getString("status").equals("success")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject itemObj = dataArray.getJSONObject(i);
                        queueItemList.add(new QueueItem(
                                itemObj.getInt("QueueID"),
                                itemObj.getString("name"),
                                itemObj.getString("barber"),
                                itemObj.getString("date_time"),
                                itemObj.optString("Haircut_Name", "N/A"),
                                itemObj.optString("Color_Name", "N/A"),
                                itemObj.optString("Shave_Name", "N/A"),
                                itemObj.getDouble("price")
                        ));
                    }
                } else {
                    noQueueText.setText(jsonObject.getString("message"));
                }
                if (queueItemList.isEmpty()) {
                    noQueueText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noQueueText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("FetchQueueTask", "JSON parsing error: " + e.getMessage());
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateStatusTask extends AsyncTask<String, Void, String> {

        // ❤️ ADD THIS METHOD TO SHOW THE SPINNER
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(String... params) {
            String queueId = params[0];
            String newStatus = params[1];
            try {
                URL url = new URL(Config.BASE_URL + "update_queue_status.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String data = URLEncoder.encode("QueueID", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(queueId, StandardCharsets.UTF_8) + "&" + URLEncoder.encode("new_status", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(newStatus, StandardCharsets.UTF_8);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { result.append(line); }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                Log.e("UpdateStatusTask", "Error updating status: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            loadingSpinner.setVisibility(View.GONE); // ❤️ HIDE THE SPINNER HERE
            if (result == null) {
                Toast.makeText(AdminQueue.this, "Server error.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                Toast.makeText(AdminQueue.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                if (jsonObject.getString("status").equals("success")) {
                    fetchQueueData();
                }
            } catch (JSONException e) {
                Log.e("UpdateStatusTask", "JSON parsing error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}