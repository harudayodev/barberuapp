package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HairAdapter extends RecyclerView.Adapter<HairAdapter.HairViewHolder> {

    private final List<Bitmap> hairList;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(Bitmap bitmap);
    }

    public HairAdapter(List<Bitmap> hairList, OnItemClickListener listener) {
        this.hairList = hairList;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public HairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hair, parent, false);
        return new HairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HairViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Bitmap bitmap = hairList.get(position);
        holder.imageView.setImageBitmap(bitmap);

        // Highlight selected item
        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(30f);
        border.setStroke(position == selectedPosition ? 8 : 0,
                position == selectedPosition ? Color.parseColor("#FFB300") : Color.TRANSPARENT);
        holder.imageView.setBackground(border);
        holder.imageView.setPadding(10, 10, 10, 10);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bitmap);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hairList.size();
    }

    static class HairViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public HairViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.hair_item_image);
        }
    }
}
