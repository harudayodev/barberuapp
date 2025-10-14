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

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder>
        implements SubmitReviewTask.ReviewSubmitListener {

    private final Context context;
    private final List<HistoryItem> completedList;
    private java.util.HashMap<String, JSONObject> userReviews = new java.util.HashMap<>();

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

    public void setUserReviews(java.util.HashMap<String, JSONObject> userReviews) {
        this.userReviews = userReviews;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        HistoryItem item = completedList.get(position);
        final int shopID = item.getShopID();
        final String barberName = item.getBarberName();
        String status = item.getStatus();

        final String reviewKey = shopID + "-" + barberName;

        holder.haircutName.setText(item.getHaircutName());
        holder.colorName.setText("Color: " + item.getColorName());
        holder.barberName.setText("Barber: " + item.getBarberName());
        holder.dateValue.setText("Date: " + item.getDateTime());

        if ("cancelled".equalsIgnoreCase(status)) {
            holder.ratingDetailsLayout.setVisibility(View.GONE);
            holder.submitButton.setVisibility(View.GONE);
            holder.reviewText.setVisibility(View.GONE);
            holder.arrowIcon.setVisibility(View.GONE);
            holder.editReview.setVisibility(View.GONE);
            holder.haircutName.setText(item.getHaircutName() + " (Cancelled)");
            int gray = ContextCompat.getColor(context, android.R.color.darker_gray);
            holder.haircutName.setTextColor(gray);
            holder.colorName.setTextColor(gray);
            holder.barberName.setTextColor(gray);
            holder.dateValue.setTextColor(gray);
            holder.itemView.setAlpha(0.6f);
            return;
        }

        holder.itemView.setAlpha(1f);
        holder.ratingDetailsLayout.setVisibility(View.GONE);
        holder.arrowIcon.setRotation(0f);
        holder.ratingStars.setVisibility(View.VISIBLE);
        holder.reviewText.setVisibility(View.VISIBLE);
        holder.submitButton.setVisibility(View.VISIBLE);
        holder.arrowIcon.setVisibility(View.VISIBLE);
        holder.editReview.setVisibility(View.GONE);

        holder.ratingStars.setIsIndicator(false);
        holder.ratingStars.setRating(0f);
        holder.ratingValue.setText("0.0");
        holder.reviewText.setEnabled(true);
        holder.reviewText.setText("");
        holder.submitButton.setText("Submit Rating");
        holder.submitButton.setEnabled(true);
        holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.espresso));

        if (userReviews.containsKey(reviewKey)) {
            JSONObject review = userReviews.get(reviewKey);
            try {
                //noinspection DataFlowIssue
                float stars = (float) review.getDouble("stars");
                String reviewContent = review.getString("reviewcontent");

                holder.ratingStars.setRating(stars);
                holder.ratingStars.setIsIndicator(true);
                holder.ratingValue.setText(String.format("%.1f", stars));
                holder.reviewText.setText(reviewContent);
                holder.reviewText.setEnabled(false);
                holder.submitButton.setText("Review Submitted");
                holder.submitButton.setEnabled(false);
                holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
                holder.editReview.setVisibility(View.VISIBLE);

                fadeIn(holder.ratingStars);
                fadeIn(holder.reviewText);
                fadeIn(holder.submitButton);

                holder.editReview.setOnClickListener(v -> {
                    holder.ratingStars.setIsIndicator(false);
                    holder.reviewText.setEnabled(true);
                    holder.submitButton.setText("Submit Rating");
                    holder.submitButton.setEnabled(false);
                    holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
                    Toast.makeText(context, "You can now edit your review.", Toast.LENGTH_SHORT).show();

                    holder.ratingStars.setOnRatingBarChangeListener((bar, newRating, fromUser) -> {
                        if (fromUser) {
                            holder.ratingValue.setText(String.format("%.1f", newRating));
                            enableSubmit(holder);
                        }
                    });

                    holder.reviewText.addTextChangedListener(new android.text.TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                            enableSubmit(holder);
                        }
                        @Override public void afterTextChanged(android.text.Editable s) {}
                    });
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        holder.ratingStars.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) holder.ratingValue.setText(String.format("%.1f", rating));
        });

        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.ratingDetailsLayout.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                collapse(holder.ratingDetailsLayout);
                rotateArrow(holder.arrowIcon, 180f, 0f);
            } else {
                expand(holder.ratingDetailsLayout);
                rotateArrow(holder.arrowIcon, 0f, 180f);
            }
        });

        // Submit rating
        holder.submitButton.setOnClickListener(v -> {
            float stars = holder.ratingStars.getRating();
            String reviewText = holder.reviewText.getText().toString().trim();// get barber from HistoryItem

            if (stars == 0f) {
                Toast.makeText(context, "Please select a star rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            int userID = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getInt("userID", 0);
            if (userID == 0) return;

            holder.submitButton.setEnabled(false);
            Toast.makeText(context, "Submitting review...", Toast.LENGTH_SHORT).show();

            new SubmitReviewTask(context, userID, shopID, stars, reviewText, barberName,
                    (success, submittedStars, submittedReviewContent) -> {
                        if (success) {
                            try {
                                JSONObject newReview = new JSONObject();
                                newReview.put("stars", submittedStars);
                                newReview.put("reviewcontent", submittedReviewContent);
                                // CHANGE 5: Update the local map with the composite key.
                                userReviews.put(reviewKey, newReview);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Refresh this specific item to show the "submitted" state.
                            notifyItemChanged(holder.getAdapterPosition());
                        } else {
                            // Re-enable the button on failure.
                            holder.submitButton.setEnabled(true);
                        }
                    }).execute();
        });
    }

    @SuppressLint("SetTextI18n")
    private void enableSubmit(RatingViewHolder holder) {
        holder.submitButton.setText("Submit Rating");
        holder.submitButton.setEnabled(true);
        holder.submitButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.espresso));
    }

    private void rotateArrow(ImageView arrow, float from, float to) {
        RotateAnimation rotate = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
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
            holder.editReview.setVisibility(View.VISIBLE);

            fadeIn(holder.ratingStars);
            fadeIn(holder.reviewText);
            fadeIn(holder.submitButton);
        } else {
            holder.submitButton.setEnabled(true);
        }
    }

    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(400).setListener(null);
    }

    @Override
    public void onReviewSubmitted(boolean success, float stars, String reviewContent) {}

    @Override
    public int getItemCount() {
        return completedList.size();
    }

    private void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (interpolatedTime == 1)
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
                if (interpolatedTime == 1) v.setVisibility(View.GONE);
                else {
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
        ImageView arrowIcon, editReview;

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
            editReview = itemView.findViewById(R.id.edit_review);
        }
    }
}
