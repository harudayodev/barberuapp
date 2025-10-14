package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

public class Reviews extends AppCompatActivity {

    private RecyclerView reviewsRecyclerView;
    private TextView noReviewsText;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ImageView returnButton = findViewById(R.id.returnbutton);
        ImageView homeButton = findViewById(R.id.homeview);
        reviewsRecyclerView = findViewById(R.id.reviews_recycler);
        noReviewsText = findViewById(R.id.no_reviews_text); // This is your "No rating yet" text

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        returnButton.setOnClickListener(v -> finish());
        homeButton.setOnClickListener(v -> finish());

        // Get the logged-in employee's ID, just like we planned
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int employeeId = prefs.getInt("employee_id", 0);

        if (employeeId > 0) {
            // Let's go get those reviews!
            new FetchReviewsTask().execute(employeeId);
        } else {
            Toast.makeText(this, "Could not identify employee.", Toast.LENGTH_SHORT).show();
            noReviewsText.setText("Could not load reviews.");
            noReviewsText.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchReviewsTask extends AsyncTask<Integer, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Integer... params) {
            int employeeId = params[0];
            try {
                // Connecting to our new PHP script
                URL url = new URL(Config.BASE_URL + "get_employee_reviews.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String data = URLEncoder.encode("employeeID", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(employeeId), StandardCharsets.UTF_8);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();

            } catch (Exception e) {
                Log.e("FetchReviewsTask", "Error fetching reviews: " + e.getMessage());
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                noReviewsText.setText("Error connecting to server.");
                noReviewsText.setVisibility(View.VISIBLE);
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                if ("success".equals(jsonObject.getString("status"))) {
                    JSONArray reviewsArray = jsonObject.getJSONArray("reviews");
                    List<ReviewItem> reviewList = new ArrayList<>();

                    for (int i = 0; i < reviewsArray.length(); i++) {
                        JSONObject reviewObj = reviewsArray.getJSONObject(i);
                        String customerName = reviewObj.getString("customerName");
                        String content = reviewObj.getString("reviewcontent");
                        String date = reviewObj.getString("reviewdate");
                        float stars = (float) reviewObj.getDouble("stars");
                        reviewList.add(new ReviewItem(customerName, content, date, stars));
                    }

                    if (reviewList.isEmpty()) {
                        // If there are no reviews, we show the message
                        noReviewsText.setText("No ratings yet.");
                        noReviewsText.setVisibility(View.VISIBLE);
                        reviewsRecyclerView.setVisibility(View.GONE);
                    } else {
                        // If we have reviews, we show the list!
                        noReviewsText.setVisibility(View.GONE);
                        reviewsRecyclerView.setVisibility(View.VISIBLE);
                        ReviewsAdapter adapter = new ReviewsAdapter(reviewList);
                        reviewsRecyclerView.setAdapter(adapter);
                    }
                } else {
                    noReviewsText.setText(jsonObject.getString("message"));
                    noReviewsText.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                Log.e("FetchReviewsTask", "JSON parsing error: " + e.getMessage());
                noReviewsText.setText("Error parsing data.");
                noReviewsText.setVisibility(View.VISIBLE);
            }
        }
    }
}