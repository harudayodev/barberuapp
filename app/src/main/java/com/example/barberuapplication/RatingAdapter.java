package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder>
        implements SubmitReviewTask.ReviewSubmitListener {

    private final Context context;
    private final List<HistoryItem> completedList;

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

    // In RatingAdapter.java

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        HistoryItem item = completedList.get(position);
        final int shopID = item.getShopID();
        String status = item.getStatus();

        holder.haircutName.setText(item.getHaircutName());
        holder.colorName.setText("Color: " + item.getColorName());
        holder.barberName.setText("Barber: " + item.getBarberName());
        holder.dateValue.setText("Date: " + item.getDateTime());

        // --- Handle Cancelled ---
        if ("cancelled".equalsIgnoreCase(status)) {
            holder.ratingDetailsLayout.setVisibility(View.GONE);
            holder.submitButton.setVisibility(View.GONE);
            holder.reviewText.setVisibility(View.GONE);
            holder.arrowIcon.setVisibility(View.GONE);

            holder.haircutName.setText(item.getHaircutName() + " (Cancelled)");
            int gray = ContextCompat.getColor(context, android.R.color.darker_gray);
            holder.haircutName.setTextColor(gray);
            holder.colorName.setTextColor(gray);
            holder.barberName.setTextColor(gray);
            holder.dateValue.setTextColor(gray);
            holder.itemView.setAlpha(0.6f);
            return; // Exit here for cancelled items
        }

        // --- Completed items (reviewable) ---
        holder.itemView.setAlpha(1f);
        holder.ratingDetailsLayout.setVisibility(View.VISIBLE);
        holder.ratingStars.setVisibility(View.VISIBLE);
        holder.reviewText.setVisibility(View.VISIBLE);
        holder.submitButton.setVisibility(View.VISIBLE);
        holder.arrowIcon.setVisibility(View.VISIBLE);

        holder.ratingStars.setRating(0f);
        holder.ratingValue.setText("0.0"); // Reset text view
        holder.reviewText.setText("");
        holder.submitButton.setText("Submit Rating");
        holder.submitButton.setEnabled(true);
        holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.espresso));

        // ******************** MOVED THE LISTENER HERE ********************
        // This listener updates the TextView whenever the user changes the star rating.
        holder.ratingStars.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                holder.ratingValue.setText(String.format("%.1f", rating));
            }
        });
        // *****************************************************************

        // Toggle expansion
        View.OnClickListener toggleListener = v -> toggleExpand(holder);
        holder.haircutName.setOnClickListener(toggleListener);
        holder.arrowIcon.setOnClickListener(toggleListener);

        // Submit rating
        holder.submitButton.setOnClickListener(v -> {
            float stars = holder.ratingStars.getRating();
            String reviewText = holder.reviewText.getText().toString().trim();

            if (stars == 0f) {
                Toast.makeText(context, "Please select a star rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            int userID = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getInt("userID", 0);

            if (userID == 0) {
                Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_LONG).show();
                return;
            }

            new SubmitReviewTask(context, userID, shopID, stars, reviewText,
                    (success, submittedStars, submittedReviewContent) -> {
                        if (v.getTag() instanceof Integer && (Integer) v.getTag() == position) {
                            handleReviewSubmissionUI(holder, success, submittedStars, submittedReviewContent);
                        }
                    }).execute();

            holder.submitButton.setEnabled(false);
            Toast.makeText(context, "Submitting review...", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void handleReviewSubmissionUI(RatingViewHolder holder, boolean success, float stars, String reviewContent) {
        if (success) {
            holder.ratingStars.setRating(stars);
            holder.ratingStars.setIsIndicator(true);
            holder.reviewText.setText(reviewContent);
            holder.reviewText.setEnabled(false);
            holder.submitButton.setText("Review Submitted");
            holder.submitButton.setEnabled(false);
            holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
        } else {
            // Keep button enabled UNLESS it's a duplicate review
            if (reviewContent.contains("already submitted")) {
                holder.submitButton.setText("Already Reviewed");
                holder.submitButton.setEnabled(false);
                holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
            } else {
                holder.submitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onReviewSubmitted(boolean success, float stars, String reviewContent) {
    }

    @Override
    public int getItemCount() {
        return completedList.size();
    }

    private void toggleExpand(RatingViewHolder holder) {
        boolean isExpanded = holder.ratingDetailsLayout.getVisibility() == View.VISIBLE;
        if (isExpanded) {
            collapse(holder.ratingDetailsLayout);
        } else {
            expand(holder.ratingDetailsLayout);
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
                v.getLayoutParams().height =
                        interpolatedTime == 1 ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
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

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView haircutName, colorName, barberName, dateValue, ratingValue;
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
            ratingValue = itemView.findViewById(R.id.rating_value);
        }
    }
}