package com.example.barberuapplication;

public class QueueItem {
    private int queueID;
    private String name;
    private String barber;
    private String dateTime;
    private String haircutName;
    private String haircutColor;

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