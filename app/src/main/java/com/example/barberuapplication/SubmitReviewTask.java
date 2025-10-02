package com.example.barberuapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SubmitReviewTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private int userId;
    private float stars;
    private String reviewContent;

    public SubmitReviewTask(Context context, int userId, float stars, String reviewContent) {
        this.context = context;
        this.userId = userId;
        this.stars = stars;
        this.reviewContent = reviewContent;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("http://192.168.100.10/submit_review.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "userID=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
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

            JSONObject jsonResponse = new JSONObject(sb.toString());
            return jsonResponse.getString("status"); // returns "success" or "fail"

        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if ("success".equals(result)) {
            Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to submit review", Toast.LENGTH_SHORT).show();
        }
    }
}
