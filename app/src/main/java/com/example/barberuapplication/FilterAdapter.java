package com.example.barberuapplication;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private final int[] filterRes;
    private final OnFilterClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Track selected filter

    public interface OnFilterClickListener {
        void onFilterClick(int resId, int position); // Pass position to the activity
    }

    public FilterAdapter(int[] filterRes, OnFilterClickListener listener) {
        this.filterRes = filterRes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        int resId = filterRes[position];

        Glide.with(holder.imageView.getContext())
                .load(filterRes[position])
                .centerInside()
                .into(holder.imageView);

        // Apply border if selected
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.filter_selected_border);

            // 🔸 Animate highlight fade-in
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f);
            fadeIn.setDuration(250);
            fadeIn.start();
        } else {
            holder.itemView.setBackgroundResource(0);

            // 🔸 Animate fade-out for deselection
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0.7f);
            fadeOut.setDuration(150);
            fadeOut.start();
        }

        holder.itemView.setOnClickListener(v -> {
            // No need to track position here, let the activity handle it
            listener.onFilterClick(resId, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return filterRes.length;
    }

    // New method to set the selected position and refresh the view
    public void setSelectedPosition(int position) {
        int previousPosition = this.selectedPosition;
        this.selectedPosition = position;

        // Notify both the previous and new selected items to update their state
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        if (this.selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(this.selectedPosition);
        }
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.filter_image);
        }
    }
}