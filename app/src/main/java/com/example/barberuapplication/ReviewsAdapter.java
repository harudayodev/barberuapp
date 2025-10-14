package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private final List<ReviewItem> reviewList;

    public ReviewsAdapter(List<ReviewItem> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We're using the item_employee_review layout for each card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem item = reviewList.get(position);

        // Set all the lovely details for the review card
        holder.customerName.setText("Reviewed by: " + item.getCustomerName());
        holder.reviewContent.setText(item.getReviewContent());
        holder.ratingBar.setRating(item.getStars());

        // We'll just show the date, not the time, to keep it clean
        String dateOnly = item.getReviewDate().split(" ")[0];
        holder.reviewDate.setText(dateOnly);

        // This makes the review content expand and collapse when you tap it!
        holder.itemView.setOnClickListener(v -> {
            boolean isVisible = holder.reviewContent.getVisibility() == View.VISIBLE;
            holder.reviewContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, reviewContent, reviewDate;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name_text);
            reviewContent = itemView.findViewById(R.id.review_content_text);
            reviewDate = itemView.findViewById(R.id.review_date_text);
            ratingBar = itemView.findViewById(R.id.review_stars);
        }
    }
}