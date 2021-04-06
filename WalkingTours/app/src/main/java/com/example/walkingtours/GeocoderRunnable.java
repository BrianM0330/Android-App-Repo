package com.example.walkingtours;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;

public class GeocoderRunnable implements Runnable{
    MapsActivity main;
    Geocoder geocoder;
    Location coordinates;

    GeocoderRunnable(MapsActivity m, Location c, Geocoder g) {
        main = m;
        coordinates = c;
        geocoder = g;
    }

    @Override
    public void run() {
        try {
            String locationString;
            List<Address> addresses;

            double latitude = coordinates.getLatitude();
            double longitude = coordinates.getLongitude();

            addresses = geocoder.getFromLocation(latitude, longitude, 10);
            String address1 = addresses.get(0).getAddressLine(0);

            locationString = String.format("%s", address1);

            if (!locationString.equals("") && locationString != null)
                main.runOnUiThread(() -> main.setAddressToText(locationString));
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}
