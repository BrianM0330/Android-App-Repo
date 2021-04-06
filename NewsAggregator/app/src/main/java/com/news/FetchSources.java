package com.news;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSources implements Runnable {

    private MainActivity activity;
    private String API_KEY = "c0b13c5da1dd4e6bbbee3f587cf1e9a1";

    FetchSources(MainActivity act) {
        activity = act;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        JSONObject responseObject;
        Uri.Builder uri = new Uri.Builder();
        uri
                .scheme("http")
                .authority("newsapi.org")
                .path("v2/sources")
                .appendQueryParameter("apiKey", API_KEY);
        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent", "");
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
                responseObject = new JSONObject(responseData.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.parseSources(responseObject);
                    }
                });
            }
            else {
                Log.d("FetchSources", conn.getResponseMessage());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
