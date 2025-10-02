package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.RotateAnimation;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> historyList;

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

        // 🔹 Dynamic title
        String title;
        if (currentItem.getStatus().equalsIgnoreCase("Completed")) {
            title = currentItem.getHaircutName() + " (Completed)";
        } else if (currentItem.getStatus().equalsIgnoreCase("Cancelled")) {
            title = currentItem.getHaircutName() + " (Cancelled)";
        } else {
            title = "💈 " + currentItem.getHaircutName();
        }
        holder.titleTextView.setText(title);

        // Apply bold-gray style to all labels
        holder.haircutName.setText(makeStyledText("Haircut: ", currentItem.getHaircutName()));
        holder.colorName.setText(makeStyledText("Color: ",
                (currentItem.getColorName() == null ? "None" : currentItem.getColorName())));
        holder.barberName.setText(makeStyledText("Barber: ", currentItem.getBarberName()));
        holder.priceValue.setText(makeStyledText("Price: ₱", String.valueOf(currentItem.getPrice())));
        holder.dateValue.setText(makeStyledText("Date: ", currentItem.getDateTime()));

        // ---- Status text with color + bold ----
        String status = currentItem.getStatus();
        String statusText = "Status: " + status;

        SpannableString spannable = new SpannableString(statusText);

        int start = statusText.indexOf(status);
        int end = start + status.length();

        int defaultColor = 0xFF555555; // gray for labels

        // "Status:" part -> gray + bold
        spannable.setSpan(new ForegroundColorSpan(defaultColor),
                0, start, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                0, start, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply color + bold only to the status value
        int statusColor;
        if (status.equalsIgnoreCase("Completed")) {
            statusColor = 0xFF4CAF50; // Green
        } else if (status.equalsIgnoreCase("Cancelled")) {
            statusColor = 0xFFF44336; // Red
        } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Ongoing")) {
            statusColor = 0xFFFFC107; // Amber
        } else {
            statusColor = defaultColor;
        }

        spannable.setSpan(new ForegroundColorSpan(statusColor),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.statusValue.setText(spannable);

        // 🔹 Toggle expand/collapse + rotate arrow
        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.detailsLayout.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                collapse(holder.detailsLayout);
                rotateArrow(holder.arrowIcon, 180f, 0f);
            } else {
                expand(holder.detailsLayout);
                rotateArrow(holder.arrowIcon, 0f, 180f);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public LinearLayout detailsLayout;
        public TextView haircutName, colorName, barberName, priceValue, dateValue, statusValue;
        public ImageView arrowIcon;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.history_item_title);
            detailsLayout = itemView.findViewById(R.id.history_item_details);
            arrowIcon = itemView.findViewById(R.id.arrow_icon);

            haircutName = itemView.findViewById(R.id.haircut_name);
            colorName = itemView.findViewById(R.id.color_name);
            barberName = itemView.findViewById(R.id.barber_name);
            priceValue = itemView.findViewById(R.id.price_value);
            dateValue = itemView.findViewById(R.id.date_value);
            statusValue = itemView.findViewById(R.id.status_value);
        }
    }

    // 🔹 Helper: Makes "Label:" gray + bold, and value normal
    private SpannableString makeStyledText(String label, String value) {
        String fullText = label + value;
        SpannableString spannable = new SpannableString(fullText);

        int defaultColor = 0xFF555555; // Gray for labels
        spannable.setSpan(new ForegroundColorSpan(defaultColor),
                0, label.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                0, label.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    private void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }
        };
        a.setDuration(300);
        v.startAnimation(a);
    }

    private void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }
        };
        a.setDuration(300);
        v.startAnimation(a);
    }

    // 🔹 Arrow rotation animation
    private void rotateArrow(ImageView arrow, float from, float to) {
        RotateAnimation rotate = new RotateAnimation(
                from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }
}
