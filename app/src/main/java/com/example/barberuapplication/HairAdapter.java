package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HairAdapter extends RecyclerView.Adapter<HairAdapter.HairViewHolder> {

    private final List<Bitmap> hairList; // Change to Bitmap
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Bitmap bitmap); // Change to Bitmap
    }

    public HairAdapter(List<Bitmap> hairList, OnItemClickListener listener) { // Change to Bitmap
        this.hairList = hairList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hair, parent, false);
        return new HairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HairViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Bitmap bitmap = hairList.get(position); // Get Bitmap from the list
        holder.imageView.setImageBitmap(bitmap); // Set the Bitmap to the ImageView
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