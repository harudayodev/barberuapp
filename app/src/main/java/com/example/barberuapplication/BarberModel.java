package com.example.barberuapplication;

public class BarberModel {
    private final String employeeID;
    private final String firstName;
    private final String lastName;
    private final String availabilityDay;
    private final boolean isAvailable;

    public BarberModel(String employeeID, String firstName, String lastName, String availabilityDay, boolean isAvailable) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.availabilityDay = availabilityDay;
        this.isAvailable = isAvailable;
    }

    public String getEmployeeID() { return employeeID; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvailabilityDay() { return availabilityDay; }
    public boolean isAvailable() { return isAvailable; }
}
