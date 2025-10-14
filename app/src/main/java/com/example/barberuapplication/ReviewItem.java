package com.example.barberuapplication;

public class ReviewItem {
    private final String customerName;
    private final String reviewContent;
    private final String reviewDate;
    private final float stars;

    public ReviewItem(String customerName, String reviewContent, String reviewDate, float stars) {
        this.customerName = customerName;
        this.reviewContent = reviewContent;
        this.reviewDate = reviewDate;
        this.stars = stars;
    }

    // Getters for all our data
    public String getCustomerName() {
        return customerName;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public float getStars() {
        return stars;
    }
}