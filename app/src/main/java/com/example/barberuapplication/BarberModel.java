package com.example.barberuapplication;

public class BarberModel {
    private final String employeeID;
    private final String firstName;
    private final String lastName;
    private final String availabilityDay;
    private final boolean isAvailable;
    // ADDED: Fields for rating data
    private final float averageRating;
    private final int reviewCount;

    // MODIFIED: Updated constructor to accept rating data
    public BarberModel(String employeeID, String firstName, String lastName, String availabilityDay, boolean isAvailable, float averageRating, int reviewCount) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.availabilityDay = availabilityDay;
        this.isAvailable = isAvailable;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public String getEmployeeID() { return employeeID; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvailabilityDay() { return availabilityDay; }
    public boolean isAvailable() { return isAvailable; }

    // ADDED: Getters for the new fields
    public float getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
}