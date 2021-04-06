package com.example.walkingtours;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Fence implements Serializable {

    private final String buildingName;
    private final String address;

    private final double lat;
    private final double lon;
    private final float radius;

    private final String description;
    private final String imageURL;

    private final String fenceColor;

    Fence(String id, String address, double lat, double lon, float radius, String description, String imageURL, String fenceColor) {
        this.buildingName = id;
        this.address = address;

        this.lat = lat;
        this.lon = lon;
        this.radius = radius;

        this.description = description;
        this.imageURL = imageURL;
        this.fenceColor = fenceColor;
    }

    String getBuildingName() {
        return buildingName;
    }

    String getAddress() {
        return address;
    }

    float getRadius() {
        return radius;
    }

    String getFenceColor() {
        return fenceColor;
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }


    @NonNull
    @Override
    public String toString() {
        return "FenceData{" +
                "id='" + buildingName + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", radius=" + radius +
                ", fenceColor='" + fenceColor + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }
}
