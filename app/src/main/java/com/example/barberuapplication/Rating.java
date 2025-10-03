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

public class Rating extends AppCompatActivity {

    private RecyclerView ratingRecyclerView;
    private RatingAdapter ratingAdapter;
    private List<HistoryItem> completedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ImageView retbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);

        retbutton.setOnClickListener(v -> finish());

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(Rating.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

        ratingRecyclerView = findViewById(R.id.rating_recycler);
        ratingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
// get logged-in user

        String customername = getSharedPreferences("UserPrefs", MODE_PRIVATE)

                .getString("fullname", "User");



// fetch completed appointments

        new FetchCompletedTask(customername).execute();

    }



    @SuppressLint("StaticFieldLeak")

    private class FetchCompletedTask extends AsyncTask<Void, Void, String> {

        private final String customername;



        FetchCompletedTask(String customername) {

            this.customername = customername;

        }



        @Override

        protected String doInBackground(Void... voids) {

            try {

                URL url = new URL(Config.BASE_URL + "get_history.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");

                conn.setDoOutput(true);



                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("customername", "UTF-8") + "=" +

                        URLEncoder.encode(customername, "UTF-8");

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



                return sb.toString();

            } catch (Exception e) {

                Log.e("FetchCompletedTask", "Error: " + e.getMessage());

                return "Error: " + e.getMessage();

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



// only include COMPLETED

                            if ("completed".equalsIgnoreCase(status)) {

                                int id = obj.getInt("id");

                                String haircutName = obj.getString("haircutName");

                                String colorName = obj.optString("colorName", "None");

                                String barberName = obj.optString("barberName", "N/A");

                                String price = obj.optString("price", "0.00");

                                String dateTime = obj.getString("dateTime");



                                completedList.add(new HistoryItem(

                                        id, haircutName, colorName, barberName,

                                        price, dateTime, status

                                ));

                            }

                        }

                        if (!completedList.isEmpty()) {

                            ratingAdapter = new RatingAdapter(Rating.this, completedList);

                            ratingRecyclerView.setAdapter(ratingAdapter);

                        } else {

                            ratingRecyclerView.setVisibility(View.GONE);

                            Toast.makeText(Rating.this,

                                    "No completed appointments to review.", Toast.LENGTH_SHORT).show();

                        }

                    }

                } catch (JSONException e) {

                    Log.e("FetchCompletedTask", "Parse error: " + e.getMessage());

                }

            }

        }

    }

}