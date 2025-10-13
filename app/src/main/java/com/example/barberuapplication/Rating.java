package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class Rating extends AppCompatActivity {

    private RecyclerView ratingRecyclerView;
    private TextView noServiceText; // Declare the new TextView

    /** @noinspection FieldCanBeLocal*/
    private RatingAdapter ratingAdapter;

    /** @noinspection FieldCanBeLocal*/
    private List<HistoryItem> completedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ImageView retbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        retbutton.setOnClickListener(v -> finish());
        homebutton.setOnClickListener(v -> finish());

        ratingRecyclerView = findViewById(R.id.rating_recycler);
        ratingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        noServiceText = findViewById(R.id.no_service_text); // Find the TextView

        // Logged-in user
        String customername = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("fullname", "User");

        // Fetch completed appointments
        new FetchCompletedTask(customername).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchCompletedTask extends AsyncTask<Void, Void, String> {
        private final String customername;

        FetchCompletedTask(String customername) {
            this.customername = customername;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(Config.BASE_URL + "get_history.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("customername", StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(customername, StandardCharsets.UTF_8);
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                reader.close();
                conn.disconnect();

                return sb.toString();
            } catch (Exception e) {
                Log.e("FetchCompletedTask", "Error: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }

        @SuppressLint("StaticFieldLeak")
        private class FetchUserReviewsTask extends AsyncTask<Void, Void, String> {
            private final int userID;

            FetchUserReviewsTask(int userID) {
                this.userID = userID;
            }

            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(Config.BASE_URL + "get_reviews.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    String data = "userID=" + URLEncoder.encode(String.valueOf(userID), StandardCharsets.UTF_8);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line);
                    reader.close();
                    conn.disconnect();

                    return sb.toString();

                } catch (Exception e) {
                    Log.e("FetchUserReviewsTask", "Error: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(String result) {
                if (result == null || result.startsWith("Error:")) return;

                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if ("success".equals(jsonResponse.getString("status"))) {
                        JSONObject reviewsObj = jsonResponse.getJSONObject("reviews");
                        java.util.HashMap<Integer, JSONObject> reviewMap = new java.util.HashMap<>();

                        Iterator<String> keys = reviewsObj.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            JSONObject obj = reviewsObj.getJSONObject(key);
                            reviewMap.put(Integer.parseInt(key), obj);
                        }

                        // Once both lists are ready, update adapter
                        if (ratingAdapter != null) {
                            ratingAdapter.setUserReviews(reviewMap);
                            ratingAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    Log.e("FetchUserReviewsTask", "Parse error: " + e.getMessage());
                }
            }
        }


        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.startsWith("Error:")) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if ("success".equals(jsonResponse.getString("status"))) {
                        completedList = new ArrayList<>();
                        JSONArray historyArray = jsonResponse.getJSONArray("history");

                        for (int i = 0; i < historyArray.length(); i++) {
                            JSONObject obj = historyArray.getJSONObject(i);
                            String status = obj.getString("status");

                            if ("completed".equalsIgnoreCase(status)) {
                                int shopID = obj.getInt("shopID");
                                String haircutName = obj.getString("haircutName");
                                String colorName = obj.optString("colorName", "None");
                                String barberName = obj.optString("barberName", "N/A");
                                String price = obj.optString("price", "0.00");
                                String dateTime = obj.getString("dateTime");

                                completedList.add(new HistoryItem(
                                        shopID, haircutName, colorName, barberName,
                                        price, dateTime, status
                                ));
                            }
                        }

                        if (!completedList.isEmpty()) {
                            ratingRecyclerView.setVisibility(View.VISIBLE);
                            noServiceText.setVisibility(View.GONE); // Hide "No service" text

                            ratingAdapter = new RatingAdapter(Rating.this, completedList);
                            ratingRecyclerView.setAdapter(ratingAdapter);

                            int userID = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    .getInt("userID", 0);
                            new FetchUserReviewsTask(userID).execute();
                        } else {
                            ratingRecyclerView.setVisibility(View.GONE);
                            noServiceText.setVisibility(View.VISIBLE); // Show "No service" text
                            // Remove the Toast message to avoid duplicates
                        }
                    }
                } catch (JSONException e) {
                    Log.e("FetchCompletedTask", "Parse error: " + e.getMessage());
                }
            }
        }
    }
}