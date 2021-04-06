package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginAPIRunnable implements Runnable{
    public final String TAG = "loginRunnable";
    public static final String ENDPOINT = "christopherhield.org";

    String username;
    String password;
    String API_KEY;
    MainActivity main;

    LoginAPIRunnable(MainActivity m, String uname, String pword, String API_KEY) {
        this.main = m;
        this.username = uname;
        this.password = pword;
        this.API_KEY = API_KEY;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        Uri.Builder uri = new Uri.Builder();
        uri
            .scheme("http")
            .authority(ENDPOINT)
            .path("api/Profile/Login")
            .appendQueryParameter("userName", username)
            .appendQueryParameter("password", password)
            .build();
        Log.d(TAG, "Constructed URI: " + uri.toString());

        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ApiKey", API_KEY);
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.catchHTMLCodes(HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                });
            }
            else if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.catchHTMLCodes(HttpURLConnection.HTTP_UNAUTHORIZED);
                    }
                });
            }
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }

            JSONObject responseJSON = new JSONObject(responseData.toString());

            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.SuccessfulLoginRequest(responseJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) { Log.e(TAG, "error in run", e);}
    }
}
