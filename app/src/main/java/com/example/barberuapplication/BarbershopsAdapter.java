package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BarbershopsAdapter extends RecyclerView.Adapter<BarbershopsAdapter.ViewHolder> {

    private final Context context;
    private final List<BarbershopModel> barbershopList;
    private final Set<String> animatedItems = new HashSet<>();
    private final String selectedHaircutID; // ✅ final since it won’t change after initialization

    // ✅ Single constructor (includes selectedHaircutID)
    public BarbershopsAdapter(Context context, List<BarbershopModel> barbershopList, String selectedHaircutID) {
        this.context = context;
        this.barbershopList = barbershopList;
        this.selectedHaircutID = selectedHaircutID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_barbershops, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarbershopModel shop = barbershopList.get(position);

        holder.tvBarbershopName.setText(shop.getName());
        holder.tvDetails.setText(shop.getAddress());

        // ✅ Status Chip Logic
        if ("active".equalsIgnoreCase(shop.getStatus())) {
            holder.tvStatus.setText("Open");
            holder.tvStatus.setBackgroundResource(R.drawable.status_chip_open);
            holder.tvStatus.setTextColor(Color.WHITE);
        } else {
            holder.tvStatus.setText("Closed");
            holder.tvStatus.setBackgroundResource(R.drawable.status_chip_closed);
            holder.tvStatus.setTextColor(Color.WHITE);
        }

        // ADDED: This logic sets the rating and hides the view if there are no reviews.
        if (shop.getReviewCount() > 0) {
            holder.ratingLayout.setVisibility(View.VISIBLE);
            holder.shopRatingBar.setRating(shop.getAverageRating());

            String reviewText = shop.getReviewCount() == 1 ? "review" : "reviews";
            String ratingText = String.format("%.1f (%d %s)",
                    shop.getAverageRating(),
                    shop.getReviewCount(),
                    reviewText);
            holder.tvRatingText.setText(ratingText);
        } else {
            // If no reviews, hide the entire rating section.
            holder.ratingLayout.setVisibility(View.GONE);
        }

        // ✅ Directions Button → Google Maps
        holder.btnDirections.setOnClickListener(v -> {
            double lat = shop.getLatitude();
            double lng = shop.getLongitude();
            String label = Uri.encode(shop.getName());
            Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + label + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });

        // ✅ Display availability based on mode
        if (context instanceof BarberShopStorePicker) {
            BarberShopStorePicker activity = (BarberShopStorePicker) context;

            if ("camera".equals(activity.getMode())) {
                holder.tvHaircutAvailable.setVisibility(View.VISIBLE);

                if (selectedHaircutID != null) {
                    checkHaircutAvailability(shop.getShopID(), selectedHaircutID, holder.tvHaircutAvailable, holder.btnMenu);
                }
            } else {
                holder.tvHaircutAvailable.setVisibility(View.GONE);
            }

            // Hide "Choose" button if homepage mode
            holder.btnMenu.setVisibility("homepage".equals(activity.getMode()) ? View.GONE : View.VISIBLE);
        }

        // ✅ Choose button click logic (placeholder — add your logic here)
        holder.btnMenu.setOnClickListener(v -> {
            if (context != null) {
                Intent intent = new Intent(context, BarberPicker.class);
                intent.putExtra("shopID", shop.getShopID());
                intent.putExtra("shopName", shop.getName());
                if (context instanceof BarberShopStorePicker) {
                    intent.putExtra("selectedHaircutName", ((BarberShopStorePicker) context).getIntent().getStringExtra("selectedHaircutName"));
                }
                context.startActivity(intent);
            }
        });

    }

    // ✅ Haircut Availability Checker
    private void checkHaircutAvailability(String shopID, String haircutID, TextView textView, MaterialButton btnMenu) {
        String url = Config.BASE_URL + "check_haircut_availability.php?shopID=" + shopID + "&haircutID=" + haircutID;
        com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);

        @SuppressLint("SetTextI18n")
        com.android.volley.toolbox.JsonObjectRequest request =
                new com.android.volley.toolbox.JsonObjectRequest(
                        com.android.volley.Request.Method.GET,
                        url,
                        null,
                        response -> {
                            try {
                                boolean available = response.getBoolean("available");
                                if (available) {
                                    // ✅ Match tvStatus style for available haircut
                                    textView.setText("Haircut available here");
                                    textView.setBackgroundResource(R.drawable.status_chip_open);
                                    textView.setTextColor(Color.WHITE);

                                    // Enable button
                                    btnMenu.setEnabled(true);
                                    btnMenu.setAlpha(1f);
                                } else {
                                    // ✅ Match tvStatus style for unavailable haircut
                                    textView.setText("Haircut not available here");
                                    textView.setBackgroundResource(R.drawable.status_chip_closed);
                                    textView.setTextColor(Color.WHITE);

                                    // Disable button + fade
                                    btnMenu.setEnabled(false);
                                    btnMenu.setAlpha(0.6f);
                                }
                            } catch (JSONException e) {
                                textView.setText("Error checking haircut");
                                textView.setBackgroundColor(Color.LTGRAY);
                                textView.setTextColor(Color.DKGRAY);
                                btnMenu.setEnabled(false);
                                btnMenu.setAlpha(0.6f);
                            }
                        },
                        error -> {
                            textView.setText("Connection error");
                            textView.setBackgroundColor(Color.LTGRAY);
                            textView.setTextColor(Color.DKGRAY);
                            btnMenu.setEnabled(false);
                            btnMenu.setAlpha(0.6f);
                        });

        queue.add(request);
    }



    @Override
    public int getItemCount() {
        return barbershopList.size();
    }

    // ✅ ViewHolder Class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBarbershop;
        TextView tvBarbershopName, tvStatus, tvDetails, tvHaircutAvailable;
        MaterialButton btnDirections, btnMenu;

        // ADDED: References for the rating UI elements
        LinearLayout ratingLayout;
        RatingBar shopRatingBar;
        TextView tvRatingText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBarbershop = itemView.findViewById(R.id.imgBarbershop);
            tvBarbershopName = itemView.findViewById(R.id.tvBarbershopName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvHaircutAvailable = itemView.findViewById(R.id.tvHaircutAvailable);
            btnDirections = itemView.findViewById(R.id.btnDirections);
            btnMenu = itemView.findViewById(R.id.btnMenu);

            // ADDED: Find the new views by their IDs
            ratingLayout = itemView.findViewById(R.id.ratingLayout);
            shopRatingBar = itemView.findViewById(R.id.shopRatingBar);
            tvRatingText = itemView.findViewById(R.id.tvRatingText);
        }
    }
}