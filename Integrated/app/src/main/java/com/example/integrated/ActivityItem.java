package com.example.integrated;

public class ActivityItem {
    private String name;
    //private String description;
    private int imageResourceId;

    // Constructor, Getters and Setters
    public ActivityItem(String name, int imageResourceId) {
        this.name = name;
        //this.description = description;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

//    public String getDescription() {
//        return description;
//    }

    public int getImageResourceId() { return imageResourceId; }
}
