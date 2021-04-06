package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateProfileAPIRunnable implements Runnable{
    public final String TAG = "CreateProfileRunnable";
    public static final String ENDPOINT = "christopherhield.org";
    private CreateProfile createProfileActivity;

    private String imageData;
    private String username;
    private String password;
    private String fname;
    private String lname;
    private String department;
    private String title;
    private String biography;
    private String API_KEY;
    private String location;

    CreateProfileAPIRunnable(CreateProfile m, String imagebytes, String uname, String pword, String fname, String lname, String department, String title, String biography, String API_KEY, String location) {
        this.createProfileActivity = m;
        this.imageData = imagebytes;
        this.username = uname;
        this.password = pword;
        this.fname = fname;
        this.lname = lname;
        this.department = department;
        this.title = title;
        this.biography = biography;
        this.API_KEY = API_KEY;
        this.location = location;
    }


    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        JSONObject responseJSON;

        Uri.Builder uri = new Uri.Builder();
        uri
            .scheme("http")
            .authority(ENDPOINT)
            .path("api/Profile/CreateProfile")
            .appendQueryParameter("firstName", fname)
            .appendQueryParameter("lastName", lname)
            .appendQueryParameter("userName", username)
            .appendQueryParameter("department", department)
            .appendQueryParameter("story", biography)
            .appendQueryParameter("position", title)
            .appendQueryParameter("password", password)
            .appendQueryParameter("remainingPointsToAward", "1000")
            .appendQueryParameter("location", location)
        .build();

        Log.d(TAG, "Constructed URI: " + uri.toString());

        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ApiKey", API_KEY);
            conn.connect();

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(imageData);
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
                createProfileActivity.runOnUiThread(new Runnable () {
                    @Override
                    public void run() {
                        createProfileActivity.catchHTMLCodes(HttpURLConnection.HTTP_CONFLICT);
                    }
                });
            }

            else if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                createProfileActivity.runOnUiThread(new Runnable () {
                    @Override
                    public void run() {
                        createProfileActivity.catchHTMLCodes(HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                });
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            responseJSON = new JSONObject(responseData.toString());

            Log.d(TAG, String.valueOf(responseJSON));

            createProfileActivity.runOnUiThread(new Runnable () {
                @Override
                public void run() {
                    try {
                        createProfileActivity.successfulProfileCreation(responseJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) { Log.e(TAG, "error in run");}
    }
}
