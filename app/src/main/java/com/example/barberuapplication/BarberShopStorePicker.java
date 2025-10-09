package com.example.barberuapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BarberShopStorePicker extends AppCompatActivity {

    /** @noinspection FieldCanBeLocal*/
    private RecyclerView barbershopRecyclerView;
    private TextView tvNearbyStores;
    private TextInputEditText searchEditText;
    private TextInputLayout searchLayout;
    private String mode = "camera";

    private List<BarbershopModel> barbershopList;
    private List<BarbershopModel> filteredList;
    private BarbershopsAdapter adapter;

    /** @noinspection FieldCanBeLocal*/
    private String selectedHaircutID = null;

    /** @noinspection FieldCanBeLocal*/
    private String selectedHaircutName = null;

    private static final String URL = Config.BASE_URL + "store_picker.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_storepicker);

        ImageView retbutton = findViewById(R.id.return_button);
        ImageView homebutton = findViewById(R.id.homeview);

        selectedHaircutID = getIntent().getStringExtra("selectedHaircutID");
        selectedHaircutName = getIntent().getStringExtra("selectedHaircutName");
        adapter = new BarbershopsAdapter(this, filteredList, selectedHaircutID);


        retbutton.setOnClickListener(v -> finish());

        homebutton.setOnClickListener(v -> {
            Intent intent = new Intent(BarberShopStorePicker.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "camera";

        barbershopRecyclerView = findViewById(R.id.barbershopRecyclerView);
        tvNearbyStores = findViewById(R.id.tvNearbyStores);
        searchEditText = findViewById(R.id.searchEditText);
        searchLayout = findViewById(R.id.searchLayout);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        View homebar = findViewById(R.id.homebar);

        if ("camera".equals(mode)) {
            homebar.setVisibility(View.GONE);
        } else {
            homebar.setVisibility(View.VISIBLE);
        }

        if (toolbar.getNavigationIcon() != null) {
            int newSize = (int) getResources().getDimension(R.dimen.return_button_size); // e.g., 48dp
            toolbar.getNavigationIcon().setBounds(0, 0, newSize, newSize);
        }

        barbershopRecyclerView.setLayoutManager(new LinearLayoutManager(this));

// ✅ Initialize lists first
        barbershopList = new ArrayList<>();
        filteredList = new ArrayList<>();

// ✅ Use the correct constructor with selectedHaircutID
        adapter = new BarbershopsAdapter(this, filteredList, selectedHaircutID);
        barbershopRecyclerView.setAdapter(adapter);

        fetchBarbershops();
        setupSearch();
        setupClearIcon();
    }

    private void fetchBarbershops() {
        RequestQueue queue = Volley.newRequestQueue(this);

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"}) JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                response -> {
                    try {
                        int count = response.getInt("count");
                        tvNearbyStores.setText("Nearby Barbershops (" + count + ")");

                        JSONArray shopsArray = response.getJSONArray("barbershops");
                        barbershopList.clear();

                        for (int i = 0; i < shopsArray.length(); i++) {
                            JSONObject shop = shopsArray.getJSONObject(i);
                            BarbershopModel model = new BarbershopModel(
                                    shop.getString("shopID"),
                                    shop.getString("name"),
                                    shop.getString("address"),
                                    shop.getString("status"),
                                    shop.optDouble("latitude", 0),
                                    shop.optDouble("longitude", 0)
                            );
                            barbershopList.add(model);
                        }

                        filteredList.clear();
                        filteredList.addAll(barbershopList);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing JSON", e);
                    }
                },
                error -> Log.e("VOLLEY_ERROR", "Error: " + error.getMessage()));

        queue.add(request);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            private String lastQuery = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().trim().toLowerCase();

                // Avoid updating if query hasn't changed
                if (query.equals(lastQuery)) return;
                lastQuery = query;

                filteredList.clear();

                if (query.isEmpty()) {
                    filteredList.addAll(barbershopList);
                } else {
                    for (BarbershopModel shop : barbershopList) {
                        if (shop.getName().toLowerCase().contains(query) ||
                                shop.getAddress().toLowerCase().contains(query)) {
                            filteredList.add(shop);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                // Animate Nearby Barbershops count with bounce effect
                int oldCount = extractCurrentCount();
                int newCount = filteredList.size();
                animateBarbershopCount(oldCount, newCount);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupClearIcon() {
        searchLayout.setEndIconOnClickListener(v -> {
            if (searchEditText.getText() != null && !searchEditText.getText().toString().isEmpty()) {
                searchEditText.setText(""); // triggers afterTextChanged automatically
            }
        });
    }

    // Extract the current number from the TextView
    private int extractCurrentCount() {
        String text = tvNearbyStores.getText().toString().replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Animate count with subtle bounce
    @SuppressLint("SetTextI18n")
    private void animateBarbershopCount(int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(400); // smooth duration
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            tvNearbyStores.setText("Nearby Barbershops (" + value + ")");
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(@NonNull Animator animation) { }
            @Override public void onAnimationEnd(@NonNull Animator animation) {
                // Bounce effect: briefly scale up and back
                tvNearbyStores.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100)
                        .withEndAction(() -> tvNearbyStores.animate().scaleX(1f).scaleY(1f).setDuration(100));
            }
            @Override public void onAnimationCancel(@NonNull Animator animation) { }
            @Override public void onAnimationRepeat(@NonNull Animator animation) { }
        });
        animator.start();
    }

    public String getMode() {
        return mode;
    }


    // Hide keyboard if user taps outside the search
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();
        if (v != null && (ev.getAction() == MotionEvent.ACTION_DOWN)) {
            int[] outLocation = new int[2];
            v.getLocationOnScreen(outLocation);
            float x = ev.getRawX() + v.getLeft() - outLocation[0];
            float y = ev.getRawY() + v.getTop() - outLocation[1];
            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                hideKeyboard();
                v.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Close search on back button
    @Override
    public void onBackPressed() {
        if (searchEditText.hasFocus()) {
            searchEditText.clearFocus();
            hideKeyboard();
        } else {
            super.onBackPressed();
        }
    }
}
