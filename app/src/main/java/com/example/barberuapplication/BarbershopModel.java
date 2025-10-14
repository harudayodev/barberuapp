package com.example.barberuapplication;

public class BarbershopModel {
    private final String shopID;
    private final String name;
    private final String address;
    private final String status;
    private final double latitude;
    private final double longitude;
    // ADDED: Fields for rating data
    private final float averageRating;
    private final int reviewCount;

    // MODIFIED: Updated constructor to accept rating data
    public BarbershopModel(String shopID, String name, String address, String status, double latitude, double longitude, float averageRating, int reviewCount) {
        this.shopID = shopID;
        this.name = name;
        this.address = address;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    // Existing Getters...
    public String getShopID() { return shopID; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // ADDED: Getters for the new fields
    public float getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
}