package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class AdminQueueAdapter extends RecyclerView.Adapter<AdminQueueAdapter.ViewHolder> {

    private final List<QueueItem> queueItemList;
    private final OnQueueItemActionListener listener;

    public interface OnQueueItemActionListener {
        void onAcceptClicked(int queueId);
        void onDeclineClicked(int queueId);
    }

    public AdminQueueAdapter(List<QueueItem> queueItemList, OnQueueItemActionListener listener) {
        this.queueItemList = queueItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View queueView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adminqueue, parent, false);
        return new ViewHolder(queueView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueueItem currentItem = queueItemList.get(position);

        holder.customerName.setText(currentItem.getName());
        holder.haircutName.setText("Haircut: " + currentItem.getHaircutName());
        holder.colorName.setText("Color: " + currentItem.getHaircutColor());
        holder.shaveName.setText("Shave: " + currentItem.getShaveName());

        // --- CHANGED THIS LINE ---
        // Use the new helper method to format the date and time
        holder.dateTime.setText("Date & Time: " + formatDateTime(currentItem.getDateTime()));
        // --- END OF CHANGE ---

        String priceText = String.format(Locale.US, "Total Price: ₱%.2f", currentItem.getPrice());
        holder.totalPrice.setText(priceText);

        holder.haircutName.setVisibility(currentItem.getHaircutName().equals("N/A") ? View.GONE : View.VISIBLE);
        holder.colorName.setVisibility(currentItem.getHaircutColor().equals("N/A") ? View.GONE : View.VISIBLE);
        holder.shaveName.setVisibility(currentItem.getShaveName().equals("N/A") ? View.GONE : View.VISIBLE);

        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClicked(currentItem.getQueueID()));
        holder.declineButton.setOnClickListener(v -> listener.onDeclineClicked(currentItem.getQueueID()));
    }

    @Override
    public int getItemCount() {
        return queueItemList.size();
    }

    private String formatDateTime(String dateTime24) {
        if (dateTime24 == null || !dateTime24.contains(" ")) {
            return dateTime24; // Return original if format is unexpected
        }

        try {
            // Split into date and time parts, e.g., "2025-10-15" and "14:30:00"
            String[] parts = dateTime24.split(" ");
            String datePart = parts[0];
            String timePart = parts[1];

            // Split time to get hour and minute
            String[] timeParts = timePart.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Convert to 12-hour format with AM/PM
            String amPm = (hour >= 12) ? "PM" : "AM";
            int hour12 = hour % 12;
            if (hour12 == 0) {
                hour12 = 12; // Handles 12 PM and 12 AM
            }

            String formattedTime = String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm);

            // Recombine and return the final string
            return datePart + " " + formattedTime;

        } catch (Exception e) {
            // If any error occurs, just return the original string
            return dateTime24;
        }
    }
    // --- END OF ADDED METHOD ---

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, haircutName, colorName, shaveName, dateTime, totalPrice;
        Button acceptButton, declineButton;

        public ViewHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.text_customer_name);
            haircutName = itemView.findViewById(R.id.text_haircut_name);
            colorName = itemView.findViewById(R.id.text_color_name);
            shaveName = itemView.findViewById(R.id.text_shave_name);
            dateTime = itemView.findViewById(R.id.text_date_time);
            totalPrice = itemView.findViewById(R.id.text_total_price);
            acceptButton = itemView.findViewById(R.id.button_accept);
            declineButton = itemView.findViewById(R.id.button_decline);
        }
    }
}