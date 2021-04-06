package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteProfileAPIRunnable implements Runnable {

    public static final String ENDPOINT = "christopherhield.org";
    private ViewProfile profileActivity;
    private String userName;
    private String API_KEY;

    DeleteProfileAPIRunnable(ViewProfile activity, String uname, String key) {
        profileActivity = activity;
        userName = uname;
        API_KEY = key;
    }

    @Override
    public void run() {
        Uri.Builder uri = new Uri.Builder();
        uri
            .scheme("http")
            .authority(ENDPOINT)
            .path("api/Profile/DeleteProfile")
            .appendQueryParameter("userName", userName)
        .build();

        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("ApiKey", API_KEY);

            Log.d("tag", String.valueOf(conn.getResponseCode()));
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                profileActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileActivity.SuccessfulDelete(200);
                    }
                });
            }
            else {
                profileActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileActivity.SuccessfulDelete(0);
                    }
                });
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }
}
