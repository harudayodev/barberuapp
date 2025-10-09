package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BarbersAdapter extends RecyclerView.Adapter<BarbersAdapter.ViewHolder> {

    private final Context context;
    private final List<BarberModel> barberList;

    public BarbersAdapter(Context context, List<BarberModel> barberList) {
        this.context = context;
        this.barberList = barberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_barbers, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BarberModel barber = barberList.get(position);
        holder.tvBarberName.setText(barber.getFirstName() + " " + barber.getLastName());
        holder.tvAvailability.setText("Available on: " + barber.getAvailabilityDay());

        if (barber.isAvailable()) {
            holder.btnChooseBarber.setEnabled(true);
            holder.btnChooseBarber.setBackgroundTintList(context.getColorStateList(R.color.amber_500));
            holder.btnChooseBarber.setAlpha(1f);
        } else {
            holder.btnChooseBarber.setEnabled(false);
            holder.btnChooseBarber.setBackgroundTintList(context.getColorStateList(R.color.gray));
            holder.btnChooseBarber.setAlpha(0.6f);
        }

        holder.btnChooseBarber.setOnClickListener(v -> {
            String barberName = barber.getFirstName() + " " + barber.getLastName();
            String availabilityDays = barber.getAvailabilityDay(); // ✅ Add this getter in your Barber model if not existing

            Intent prevIntent = ((BarberPicker) context).getIntent();
            String haircutName = prevIntent.getStringExtra("selectedHaircutName");
            String shopName = prevIntent.getStringExtra("shopName");

            Intent intent = new Intent(context, HairstyleConfirm.class);
            intent.putExtra("selectedHaircutName", haircutName);
            intent.putExtra("shopName", shopName);
            intent.putExtra("barberName", barberName);
            intent.putExtra("availabilityDays", availabilityDays); // ✅ pass availability
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBarber;
        TextView tvBarberName, tvAvailability;
        MaterialButton btnChooseBarber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBarber = itemView.findViewById(R.id.imgBarber);
            tvBarberName = itemView.findViewById(R.id.tvBarberName);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            btnChooseBarber = itemView.findViewById(R.id.btnChooseBarber);
        }
    }
}
