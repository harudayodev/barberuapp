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

    public interface ReviewSubmitListener {
        void onReviewSubmitted(boolean success, float stars, String reviewContent);
    }

    private final Context context;
    private final int userId;
    private final int shopId;
    private final float stars;
    private final String reviewContent;
    private final ReviewSubmitListener listener;

    public SubmitReviewTask(Context context, int userId, int shopId, float stars, String reviewContent, ReviewSubmitListener listener) {
        this.context = context;
        this.userId = userId;
        this.shopId = shopId;
        this.stars = stars;
        this.reviewContent = reviewContent;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(Config.BASE_URL + "submit_review.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "userID=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
                    "&shopID=" + URLEncoder.encode(String.valueOf(shopId), "UTF-8") +
                    "&stars=" + URLEncoder.encode(String.valueOf(stars), "UTF-8") +
                    "&reviewcontent=" + URLEncoder.encode(reviewContent, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            String response = sb.toString();
            Log.d("SubmitReviewTask", "Response: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.getString("status");
            if ("fail".equals(status)) {
                return "fail: " + jsonResponse.optString("message", "Unknown error");
            }
            return status;

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

        if (listener != null) {
            listener.onReviewSubmitted(success, stars, reviewContent);
        }
    }
}