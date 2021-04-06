package com.example.rewards;

import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditProfileRunnable implements Runnable {
    public static final String ENDPOINT = "christopherhield.org";

    EditProfile activity;
    String imageData;
    String firstname;
    String lastname;
    String username;
    String department;
    String story;
    String position;
    String password;
    String location;
    String API_KEY;

    EditProfileRunnable(
            EditProfile act,
            String fname,
            String lname,
            String uname,
            String dep,
            String bio,
            String pos,
            String pword,
            String loc,
            String bytes,
            String key
    ) {
        this.activity = act;
        this.firstname = fname;
        this.lastname = lname;
        this.username = uname;
        this.department = dep;
        this.story = bio;
        this.position = pos;
        this.password = pword;
        this.location = loc;
        this.imageData = bytes;
        this.API_KEY = key;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        JSONObject responseJSON;

        Uri.Builder uri = new Uri.Builder();
        uri
                .scheme("http")
                .authority(ENDPOINT)
                .path("api/Profile/UpdateProfile")
                .appendQueryParameter("firstName", firstname)
                .appendQueryParameter("lastName", lastname)
                .appendQueryParameter("userName", username)
                .appendQueryParameter("department", department)
                .appendQueryParameter("story", story)
                .appendQueryParameter("position", position)
                .appendQueryParameter("password", password)
                .appendQueryParameter("location", location)
                .build();
        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ApiKey", API_KEY);
            conn.connect();

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(imageData);
            out.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_BAD_REQUEST) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    responseData.append(line);
                }
                responseJSON = new JSONObject(responseData.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.catchResponse(responseJSON);
                    }
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
