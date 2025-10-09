package com.example.barberuapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private final List<Haircut> haircutList;
    private final OnFilterClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnFilterClickListener {
        void onFilterClick(Haircut haircut, int position);
    }

    public List<Haircut> getHaircutList() {
        return haircutList;
    }

    public FilterAdapter(List<Haircut> haircutList, OnFilterClickListener listener) {
        this.haircutList = haircutList;
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
        Haircut haircut = haircutList.get(position);

        Glide.with(holder.imageView.getContext())
                .load(R.drawable.haircutalt)
                .centerInside()
                .into(holder.imageView);

        // Highlight selected filter
        holder.itemView.setBackgroundResource(
                position == selectedPosition ? R.drawable.filter_selected_border : 0
        );

        holder.itemView.setOnClickListener(v -> listener.onFilterClick(haircut, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return haircutList.size();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition);
        if (selectedPosition != RecyclerView.NO_POSITION) notifyItemChanged(selectedPosition);
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.filter_image);
        }
    }
}
