package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem currentItem = historyList.get(position);

        holder.titleTextView.setText("Haircut #" + (position + 1));
        holder.queueNumberTextView.setText(currentItem.getQueueNumber());
        holder.serviceNameTextView.setText(currentItem.getServiceName());
        holder.barberNameTextView.setText(currentItem.getBarberName());
        holder.priceTextView.setText(currentItem.getPrice());
        holder.dateTextView.setText(currentItem.getDate());
        holder.timeslotTextView.setText(currentItem.getTimeslot());
        holder.statusTextView.setText(currentItem.getStatus());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public LinearLayout detailsLayout;
        public TextView queueNumberTextView, serviceNameTextView, barberNameTextView, priceTextView, dateTextView, timeslotTextView, statusTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.history_item_title);
            detailsLayout = itemView.findViewById(R.id.history_item_details);

            queueNumberTextView = itemView.findViewById(R.id.queue_number_value);
            serviceNameTextView = itemView.findViewById(R.id.service_name);
            barberNameTextView = itemView.findViewById(R.id.barber_name);
            priceTextView = itemView.findViewById(R.id.price_value);
            dateTextView = itemView.findViewById(R.id.date_value);
            timeslotTextView = itemView.findViewById(R.id.timeslot_value);
            statusTextView = itemView.findViewById(R.id.status_value);

            // Set up the click listener to toggle visibility
            itemView.setOnClickListener(v -> {
                if (detailsLayout.getVisibility() == View.GONE) {
                    detailsLayout.setVisibility(View.VISIBLE);
                } else {
                    detailsLayout.setVisibility(View.GONE);
                }
            });
        }
    }
}