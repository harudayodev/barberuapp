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
import androidx.core.content.ContextCompat; // Import for color tint

import java.util.List;

// Implement the listener in the adapter to handle the callback
public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder>
        implements SubmitReviewTask.ReviewSubmitListener { // Implementing here for simple access to Context

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        HistoryItem item = completedList.get(position);

        final int salesID = item.getId();

        // ⭐ Add a tag to the ViewHolder's submit button so we can find it in the listener
        holder.submitButton.setTag(position);

        holder.haircutName.setText(item.getHaircutName());
        holder.colorName.setText("Color: " + (item.getColorName() == null ? "None" : item.getColorName()));
        holder.barberName.setText("Barber: " + item.getBarberName());
        holder.dateValue.setText("Date: " + item.getDateTime());

        // Reset state (assuming no persistent check for submitted reviews)
        // If a review has already been submitted for this salesID, you need to update
        // HistoryItem and fetch that status/rating from the server in Rating.java

        // ⭐ INITIAL/RESET STATE: Enable rating/submission
        holder.ratingStars.setRating(0f);
        holder.ratingStars.setIsIndicator(false); // Make stars editable
        holder.reviewText.setText("");
        holder.reviewText.setEnabled(true);
        holder.submitButton.setText("Submit Rating");
        holder.submitButton.setEnabled(true);
        // Reset background tint (You'll need to define @color/espresso in your resources)
        holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.espresso));


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

            int userID = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getInt("userID", 0);

            if (userID == 0) {
                Toast.makeText(context, "Error: User not logged in (userID not found).", Toast.LENGTH_LONG).show();
                return;
            }

            // ⭐ Pass 'this' (the adapter) as the listener
            new SubmitReviewTask(context, userID, salesID, stars, reviewText,
                    // Anonymous implementation of the listener to handle the result
                    (success, submittedStars, submittedReviewContent) -> {
                        // Call the method to handle UI updates on the specific item
                        if (v.getTag() instanceof Integer && (Integer) v.getTag() == position) {
                            handleReviewSubmissionUI(holder, success, submittedStars, submittedReviewContent);
                        }
                    }).execute();

            holder.submitButton.setEnabled(false);
            Toast.makeText(context, "Submitting review...", Toast.LENGTH_SHORT).show();
        });

        // Allow click on stars to update immediately
        holder.ratingStars.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) holder.ratingStars.setRating(rating);
        });
    }

    // ⭐ New method to handle the UI changes after submission
    @SuppressLint("SetTextI18n")
    private void handleReviewSubmissionUI(RatingViewHolder holder, boolean success, float stars, String reviewContent) {
        if (success) {
            // 1. Set stars to stay
            holder.ratingStars.setRating(stars);
            holder.ratingStars.setIsIndicator(true); // Make stars uneditable/indicator-only

            // 2. Display short review and make uneditable
            holder.reviewText.setText(reviewContent);
            holder.reviewText.setEnabled(false);

            // 3. Dim out and rename submit button
            holder.submitButton.setText("Review Submitted");
            holder.submitButton.setEnabled(false);
            // You will need to define @color/gray or R.attr.colorButtonNormal in your resources
            holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));

        } else {
            // Re-enable the button on failure so the user can try again
            holder.submitButton.setEnabled(true);
        }
    }

    // The implementation of the interface method is now handled inline in onBindViewHolder
    // to correctly reference the specific ViewHolder's UI elements.
    // The below is technically not needed since we used an anonymous inner class,
    // but kept here for completeness if you prefer a class-level implementation:
    @Override
    public void onReviewSubmitted(boolean success, float stars, String reviewContent) {
        // This global method is difficult to use in a RecyclerView,
        // as it doesn't know which item submitted the review.
        // We handle the UI update directly in the OnClickListener's callback above.
    }


    @Override
    public int getItemCount() {
        return completedList.size();
    }

    // [Helper methods (toggleExpand, expand, collapse, rotateArrow) and
    // RatingViewHolder remain unchanged and are omitted for brevity.]
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