package com.example.barberuapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SubmitReviewTask extends AsyncTask<Void, Void, String> {

    // Interface to communicate success/failure back to the adapter/UI
    public interface ReviewSubmitListener {
        void onReviewSubmitted(boolean success, float stars, String reviewContent);
    }

    private final Context context;
    private final int userId;
    private final int salesId;
    private final float stars;
    private final String reviewContent;
    private final ReviewSubmitListener listener; // Added listener

    // Updated constructor to accept the listener
    public SubmitReviewTask(Context context, int userId, int salesId, float stars, String reviewContent, ReviewSubmitListener listener) {
        this.context = context;
        this.userId = userId;
        this.salesId = salesId;
        this.stars = stars;
        this.reviewContent = reviewContent;
        this.listener = listener; // Store the listener
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // URL might need fixing, was 192.168.100.10/submit_review.php
            URL url = new URL(Config.BASE_URL + "submit_review.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Updated postData to include salesID
            String postData = "userID=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
                    "&salesID=" + URLEncoder.encode(String.valueOf(salesId), "UTF-8") +
                    "&stars=" + URLEncoder.encode(String.valueOf(stars), "UTF-8") +
                    "&reviewcontent=" + URLEncoder.encode(reviewContent, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            String response = sb.toString();
            Log.d("SubmitReviewTask", "Response: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.getString("status");
            if ("fail".equals(status)) {
                return "fail: " + jsonResponse.optString("message", "Unknown error");
            }
            return status; // returns "success"

        } catch (Exception e) {
            Log.e("SubmitReviewTask", "Error during submission: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        boolean success = "success".equals(result);

        if (success) {
            Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show();
        } else {
            String message = result.startsWith("fail:") ? result.substring(5) : "Failed to submit review. Please check server logs.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.e("SubmitReviewTask", "Submission failed: " + result);
        }

        // ⭐ Call the listener to update the UI
        if (listener != null) {
            listener.onReviewSubmitted(success, stars, reviewContent);
        }
    }
}