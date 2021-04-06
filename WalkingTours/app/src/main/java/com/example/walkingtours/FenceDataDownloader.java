package com.example.walkingtours;


import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class FenceDataDownloader implements Runnable {

    private static final String TAG = "FenceDataDownloader";
    private final Geocoder geocoder;
    private final FenceManager fenceManager;
    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";

    FenceDataDownloader(MapsActivity mapsActivity, FenceManager fenceManager) {
        this.fenceManager = fenceManager;
        geocoder = new Geocoder(mapsActivity.getApplicationContext());
    }

    private void processData(String result) {

        if (result == null)
            return;

        ArrayList<Fence> fences = new ArrayList<>();
        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fenceObject = jArr.getJSONObject(i);
                String buildingName = fenceObject.getString("id");
                String address = fenceObject.getString("address");
                float rad = (float) fenceObject.getDouble("radius");

                String description = fenceObject.getString("description");
                String imgURL = fenceObject.getString("image");
                String color = fenceObject.getString("fenceColor");

                LatLng ll = getLatLong(address);

                if (ll != null) {
                    Fence fd = new Fence(buildingName, address, ll.latitude, ll.longitude, rad, description, imgURL, color);
                    fences.add(fd);
                }
            }
            fenceManager.addFences(fences);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: Response code: " + connection.getResponseCode());
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            processData(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LatLng getLatLong(String address) {
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            Address a = addressList.get(0);
            return new LatLng(a.getLatitude(), a.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}