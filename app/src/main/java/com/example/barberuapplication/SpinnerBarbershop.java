package com.example.barberuapplication;

import androidx.annotation.NonNull;

public class SpinnerBarbershop {
    private final String shopID;
    private final String shopName;

    public SpinnerBarbershop(String shopID, String shopName) {
        this.shopID = shopID;
        this.shopName = shopName;
    }

    public String getShopID() {
        return shopID;
    }

    public String getShopName() {
        return shopName;
    }

    // This is crucial! The ArrayAdapter uses this method to display text in the Spinner.
    @NonNull
    @Override
    public String toString() {
        return shopName;
    }
}