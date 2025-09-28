package com.example.barberuapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

class BarbershopAdapter extends RecyclerView.Adapter<BarbershopAdapter.ViewHolder> {
    private final List<String> barbershops;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String name);
    }

    public BarbershopAdapter(List<String> barbershops, OnItemClickListener listener) {
        this.barbershops = barbershops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_barbershop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = barbershops.get(position);
        holder.textView.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(name));
    }

    @Override
    public int getItemCount() {
        return barbershops.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.barbershop_name);
        }
    }
}
