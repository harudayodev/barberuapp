package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private final Context context;
    private final List<HistoryItem> completedList; // reuse HistoryItem for haircut data

    public RatingAdapter(Context context, List<HistoryItem> completedList) {
        this.context = context;
        this.completedList = completedList;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        HistoryItem item = completedList.get(position);

        holder.haircutName.setText(item.getHaircutName());
        holder.colorName.setText("Color: " + (item.getColorName() == null ? "None" : item.getColorName()));
        holder.barberName.setText("Barber: " + item.getBarberName());
        holder.dateValue.setText("Date: " + item.getDateTime());

        // Reset previous ratings when recycling views
        holder.ratingStars.setRating(0f);
        holder.reviewText.setText("");

        // Toggle expand/collapse on title or arrow click
        View.OnClickListener toggleListener = v -> toggleExpand(holder);
        holder.haircutName.setOnClickListener(toggleListener);
        holder.arrowIcon.setOnClickListener(toggleListener);

        // Submit rating button
        holder.submitButton.setOnClickListener(v -> {
            float stars = holder.ratingStars.getRating();
            String reviewText = holder.reviewText.getText().toString().trim();

            if (stars == 0f) {
                Toast.makeText(context, "Please select a star rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Get the actual logged-in userID from SharedPreferences
            int userID = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getInt("userID", 0);

            if (userID == 0) {
                Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Send correct userID, stars, and review text
            new SubmitReviewTask(context, userID, stars, reviewText).execute();

            holder.submitButton.setEnabled(false);
            Toast.makeText(context, "Submitting review...", Toast.LENGTH_SHORT).show();
        });

        // Allow click on stars to update immediately
        holder.ratingStars.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) holder.ratingStars.setRating(rating);
        });
    }

    @Override
    public int getItemCount() {
        return completedList.size();
    }

    // -------------------- Helper Methods --------------------

    private void toggleExpand(RatingViewHolder holder) {
        boolean isExpanded = holder.ratingDetailsLayout.getVisibility() == View.VISIBLE;
        if (isExpanded) {
            collapse(holder.ratingDetailsLayout);
            rotateArrow(holder.arrowIcon, 180f, 0f);
        } else {
            expand(holder.ratingDetailsLayout);
            rotateArrow(holder.arrowIcon, 0f, 180f);
        }
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

    // -------------------- ViewHolder --------------------

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView haircutName, colorName, barberName, dateValue;
        RatingBar ratingStars;
        EditText reviewText;
        Button submitButton;
        LinearLayout ratingDetailsLayout;
        ImageView arrowIcon;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            haircutName = itemView.findViewById(R.id.haircut_name);
            colorName = itemView.findViewById(R.id.color_name);
            barberName = itemView.findViewById(R.id.barber_name);
            dateValue = itemView.findViewById(R.id.date_value);
            ratingStars = itemView.findViewById(R.id.rating_stars);
            reviewText = itemView.findViewById(R.id.review_text);
            submitButton = itemView.findViewById(R.id.submit_review);
            ratingDetailsLayout = itemView.findViewById(R.id.rating_details_layout);
            arrowIcon = itemView.findViewById(R.id.arrow_icon);
        }
    }
}
