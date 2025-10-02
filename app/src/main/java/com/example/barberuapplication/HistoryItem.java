package com.example.barberuapplication;

public class HistoryItem {
    private int id;
    private String haircutName;
    private String colorName;
    private String barberName;
    private String price;
    private String dateTime;
    private String status;

    public HistoryItem(int id, String haircutName, String colorName, String barberName,
                       String price, String dateTime, String status) {
        this.id = id;
        this.haircutName = haircutName;
        this.colorName = colorName;
        this.barberName = barberName;
        this.price = price;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getHaircutName() { return haircutName; }
    public String getColorName() { return colorName; }
    public String getBarberName() { return barberName; }
    public String getPrice() { return price; }
    public String getDateTime() { return dateTime; }
    public String getStatus() { return status; }
}
