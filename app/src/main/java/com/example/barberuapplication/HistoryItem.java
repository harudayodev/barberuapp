package com.example.barberuapplication;

public class HistoryItem {
    private final int shopID; // actual shopID
    private final String haircutName;
    private final String colorName;
    private final String barberName;
    private final String price;
    private final String dateTime;
    private final String status;

    public HistoryItem(int shopID, String haircutName, String colorName, String barberName,
                       String price, String dateTime, String status) {
        this.shopID = shopID;
        this.haircutName = haircutName;
        this.colorName = colorName;
        this.barberName = barberName;
        this.price = price;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getShopID() { return shopID; }
    public String getHaircutName() { return haircutName; }
    public String getColorName() { return colorName; }
    public String getBarberName() { return barberName; }
    public String getPrice() { return price; }
    public String getDateTime() { return dateTime; }
    public String getStatus() { return status; }
}
