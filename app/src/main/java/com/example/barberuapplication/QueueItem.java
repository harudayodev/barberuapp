package com.example.barberuapplication;

public class QueueItem {
    private final int queueID;
    private final String name;
    private final String barber;
    private final String dateTime;
    private final String haircutName;
    private final String haircutColor;
    private final String shaveName;
    private final double price; // ADDED

    // Constructor updated
    public QueueItem(int queueID, String name, String barber, String dateTime, String haircutName, String colorName, String shaveName, double price) {
        this.queueID = queueID;
        this.name = name;
        this.barber = barber;
        this.dateTime = dateTime;
        this.haircutName = haircutName;
        this.haircutColor = colorName;
        this.shaveName = shaveName;
        this.price = price; // ADDED
    }

    // Getters
    public int getQueueID() { return queueID; }
    public String getName() { return name; }
    public String getBarber() { return barber; }
    public String getDateTime() { return dateTime; }
    public String getHaircutName() { return haircutName; }
    public String getHaircutColor() { return haircutColor; }
    public String getShaveName() { return shaveName; }
    public double getPrice() { return price; } // ADDED
}