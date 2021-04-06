package com.example.walkingtours;

public class Building {

    private final String name;
    private final String imageURL;
    private final String address;
    private final String description;

    Building(String n, String i, String a, String d) {
        name = n;
        imageURL = i;
        address = a;
        description = d;
    }


    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }
}
