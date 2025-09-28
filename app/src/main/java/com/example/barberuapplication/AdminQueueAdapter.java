package com.example.barberuapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminQueueAdapter extends RecyclerView.Adapter<AdminQueueAdapter.QueueViewHolder> {

    private List<QueueItem> queueList;
    private OnActionButtonClickListener listener;

    public interface OnActionButtonClickListener {
        void onAcceptClick(int queueID, int position);
        void onDeclineClick(int queueID, int position);
    }

    public AdminQueueAdapter(List<QueueItem> queueList, OnActionButtonClickListener listener) {
        this.queueList = queueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adminqueue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        QueueItem item = queueList.get(position);

        holder.textCustomerName.setText(item.getName());

        String haircutDetail = item.getHaircutName();
        if (!item.getHaircutColor().isEmpty()) {
            haircutDetail += " (" + item.getHaircutColor() + ")";
        }
        holder.textHaircutName.setText(haircutDetail);

        // Format Barber and Time
        String barberAndTime = String.format("Barber: %s | %s", item.getBarber(), item.getDateTime());
        holder.textBarberTime.setText(barberAndTime);

        // Set click listeners for buttons
        holder.buttonAccept.setOnClickListener(v -> listener.onAcceptClick(item.getQueueID(), holder.getAdapterPosition()));
        holder.buttonDecline.setOnClickListener(v -> listener.onDeclineClick(item.getQueueID(), holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return queueList.size();
    }

    // Method to remove item from the list after action
    public void removeItem(int position) {
        queueList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, queueList.size());
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView textCustomerName;
        TextView textHaircutName;
        TextView textBarberTime;
        Button buttonAccept;
        Button buttonDecline;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            textCustomerName = itemView.findViewById(R.id.text_customer_name);
            textHaircutName = itemView.findViewById(R.id.text_haircut_name);
            textBarberTime = itemView.findViewById(R.id.text_barber_time);
            buttonAccept = itemView.findViewById(R.id.button_accept);
            buttonDecline = itemView.findViewById(R.id.button_decline);
        }
    }
}