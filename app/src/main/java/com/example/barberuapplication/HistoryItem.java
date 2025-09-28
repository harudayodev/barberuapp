package com.example.barberuapplication;

public class HistoryItem {
    private String queueNumber;
    private String customerName;
    private String serviceName;
    private String barberName;
    private String price;
    private String date;
    private String timeslot;
    private String status;

    public HistoryItem(String queueNumber, String customerName, String serviceName, String barberName, String price, String date, String timeslot, String status) {
        this.queueNumber = queueNumber;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.barberName = barberName;
        this.price = price;
        this.date = date;
        this.timeslot = timeslot;
        this.status = status;
    }

    public String getQueueNumber() { return queueNumber; }
    public String getCustomerName() { return customerName; }
    public String getServiceName() { return serviceName; }
    public String getBarberName() { return barberName; }
    public String getPrice() { return price; }
    public String getDate() { return date; }
    public String getTimeslot() { return timeslot; }
    public String getStatus() { return status; }
}