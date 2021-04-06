package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchProfilesRunnable implements Runnable{
    public final String TAG = "profilesRunnable";
    public static final String ENDPOINT = "christopherhield.org";

    Leaderboard leaderboardActivity;
    String API_KEY;

    FetchProfilesRunnable(String key, Leaderboard activity) {
        leaderboardActivity = activity;
        API_KEY = key;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        Uri.Builder uri = new Uri.Builder();
        uri
                .scheme("http")
                .authority(ENDPOINT)
                .path("api/Profile/GetAllProfiles")
                .build();
        try {
            URL url = new URL(uri.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ApiKey", API_KEY);
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Got a non-200 code");
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }

            JSONArray responseJSON = new JSONArray(responseData.toString());

            leaderboardActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        leaderboardActivity.SuccessfulGET(responseJSON);
                    } catch (Exception e) {e.printStackTrace();}
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
