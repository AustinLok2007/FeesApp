package com.example.test2;

public class Invoice {
    private String date;
    private String time;
    private String itemName;
    private double price;
    private String imagePath;
    private String userId;

    public Invoice() {
    }

    public Invoice(String date, String time, String itemName, double price, String imagePath, String userId) {
        this.date = date;
        this.time = time;
        this.itemName = itemName;
        this.price = price;
        this.imagePath = imagePath;
        this.userId = userId;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
