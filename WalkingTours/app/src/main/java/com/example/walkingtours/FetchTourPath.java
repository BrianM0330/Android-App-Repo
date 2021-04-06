package com.example.walkingtours;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchTourPath implements Runnable {
    MapsActivity mapsActivity;
    private static final String TOUR_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";

    FetchTourPath(MapsActivity m) {
        mapsActivity = m;
    }

    @Override
    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(TOUR_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            mapsActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapsActivity.addTourPoly(buffer.toString());
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
