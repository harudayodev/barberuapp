package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BarberPicker extends AppCompatActivity {

    private TextView tvAvailableBarbers;
    private List<BarberModel> barberList;
    private BarbersAdapter adapter;

    private static final String URL = Config.BASE_URL + "barber_picker.php?shopID=";
    private static final String DEFAULT_SHOP_ID = "0"; // Changed default value type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barber_picker);

        RecyclerView barbersRecyclerView = findViewById(R.id.barbersRecyclerView);
        tvAvailableBarbers = findViewById(R.id.tvAvailableBarbers);
        ImageView retbutton = findViewById(R.id.return_button);
        retbutton.setOnClickListener(v -> finish());

        barberList = new ArrayList<>();
        adapter = new BarbersAdapter(this, barberList);
        barbersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        barbersRecyclerView.setAdapter(adapter);

        // Get shopID from previous activity
        // ✅ FIX: Retrieve shopID as String, not int.
        String shopID = getIntent().getStringExtra("shopID");
        if (shopID == null || shopID.isEmpty()) {
            shopID = DEFAULT_SHOP_ID;
            Log.w("BarberPicker", "shopID was null or empty, using default: " + DEFAULT_SHOP_ID);
        }

        fetchBarbers(shopID);
    }

    // ✅ FIX: Changed shopID type to String
    private void fetchBarbers(String shopID) {
        RequestQueue queue = Volley.newRequestQueue(this);
        // ✅ The URL should now correctly include the String shopID
        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"}) JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL + shopID, null,
                response -> {
                    try {
                        int count = response.getInt("count");
                        tvAvailableBarbers.setText("Available Barbers (" + count + ")");
                        JSONArray barbersArray = response.getJSONArray("barbers");

                        barberList.clear();
                        for (int i = 0; i < barbersArray.length(); i++) {
                            JSONObject obj = barbersArray.getJSONObject(i);
                            barberList.add(new BarberModel(
                                    obj.getString("employeeID"),
                                    obj.getString("firstName"),
                                    obj.getString("lastName"),
                                    obj.getString("availabilityDay"),
                                    obj.getBoolean("isAvailable")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing response: " + e);
                        tvAvailableBarbers.setText("Error loading barbers.");
                    }
                },
                error -> {
                    Log.e("VOLLEY_ERROR", "Volley error: " + error.toString());
                    tvAvailableBarbers.setText("Connection error.");
                }
        );

        queue.add(request);
    }
}