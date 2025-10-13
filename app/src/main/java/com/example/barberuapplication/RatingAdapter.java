package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;


public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private final Context context;
    private final List<HistoryItem> completedList;

    public RatingAdapter(Context context, List<HistoryItem> completedList) {
        this.context = context;
        this.completedList = completedList;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new RatingViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        HistoryItem item = completedList.get(position);
        holder.text1.setText(item.getHaircutName());
        holder.text2.setText("Barber: " + item.getBarberName() + " | Date: " + item.getDateTime());
    }

    @Override
    public int getItemCount() {
        return completedList.size();
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
