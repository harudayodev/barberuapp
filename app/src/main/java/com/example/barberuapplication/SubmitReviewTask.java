package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SubmitReviewTask extends AsyncTask<Void, Void, String> {

    public interface ReviewSubmitListener {
        void onReviewSubmitted(boolean success, float stars, String reviewContent);
    }

    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final int userID;
    private final int shopID;
    private final float stars;
    private final String reviewContent;
    private final String barberName;
    private final ReviewSubmitListener listener;

    public SubmitReviewTask(Context context, int userID, int shopID, float stars,
                            String reviewContent, String barberName, ReviewSubmitListener listener) {
        this.context = context;
        this.userID = userID;
        this.shopID = shopID;
        this.stars = stars;
        this.reviewContent = reviewContent;
        this.barberName = barberName;
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("https://barberucuts.site/barberuapp/submit_review.php");

            String data = "userID=" + URLEncoder.encode(String.valueOf(userID), StandardCharsets.UTF_8)
                    + "&shopID=" + URLEncoder.encode(String.valueOf(shopID), StandardCharsets.UTF_8)
                    + "&stars=" + URLEncoder.encode(String.valueOf(stars), StandardCharsets.UTF_8)
                    + "&reviewcontent=" + URLEncoder.encode(reviewContent, StandardCharsets.UTF_8)
                    + "&barber=" + URLEncoder.encode(barberName, StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            br.close();
            conn.disconnect();

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject json = new JSONObject(result);
            boolean success = json.optString("status").equalsIgnoreCase("success");
            String message = json.optString("message", "No message from server.");

            if (success) {
                JSONObject data = json.optJSONObject("data");
                if (data != null && listener != null) {
                    float newStars = (float) data.optDouble("stars", stars);
                    String newReview = data.optString("reviewcontent", reviewContent);
                    listener.onReviewSubmitted(true, newStars, newReview);
                }

                Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                if (listener != null) listener.onReviewSubmitted(false, stars, message);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (listener != null) listener.onReviewSubmitted(false, stars, e.getMessage());
        }
    }
}
