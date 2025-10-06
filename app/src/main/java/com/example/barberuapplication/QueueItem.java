package com.example.barberuapplication;

public class QueueItem {
    private final int queueID;
    private final String name;
    private final String barber;
    private final String dateTime;
    private final String haircutName;
    private final String haircutColor;

    // Constructor
    public QueueItem(int queueID, String name, String barber, String dateTime, String haircutName, String haircutColor) {
        this.queueID = queueID;
        this.name = name;
        this.barber = barber;
        this.dateTime = dateTime;
        this.haircutName = haircutName;
        this.haircutColor = haircutColor;
    }

    // Getters
    public int getQueueID() { return queueID; }
    public String getName() { return name; }
    public String getBarber() { return barber; }
    public String getDateTime() { return dateTime; }
    public String getHaircutName() { return haircutName; }
    public String getHaircutColor() { return haircutColor; }
}