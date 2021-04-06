package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class createAPIKeyRunnable implements Runnable {
    public final String TAG = "createKeyRunnable";
    public static final String ENDPOINT = "christopherhield.org";
    private MainActivity mainActivity;

    private String fname;
    private String lname;
    private String email;
    private Integer id;

    createAPIKeyRunnable(MainActivity mainActivity, String fname, String lname, String email, Integer id) {
        this.mainActivity = mainActivity;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.id=id;
    }

    @Override
    public void run() {
        StringBuilder apiKeyString = new StringBuilder();
        Uri.Builder uri = new Uri.Builder();
        uri
            .scheme("http")
            .authority(ENDPOINT)
            .path("api/Profile/GetStudentApiKey")
            .appendQueryParameter("firstName", fname)
            .appendQueryParameter("lastName", lname)
            .appendQueryParameter("studentId", String.valueOf(id))
            .appendQueryParameter("email", email)
        .build();

        Log.d(TAG, "Constructed URI: " + uri.toString());

        try {
            URL url = new URL(uri.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: got a non 200 response code" + conn.getResponseCode());
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line = reader.readLine();
            apiKeyString
                    .append(line.substring(line.indexOf("\"apiKey\""))
                            .replace(":", "")
                            .replace("apiKey", "")
                            .replace("\"", "")
                            .replace("}", "")
                    );

//            while ((line = reader.readLine()) != null) {
//                int x = line.indexOf("apiKey");
//                apiKeyString.append(line.substring(x));
//                if (line.contains("apiKey")) apiKeyString.append(line);
//            }

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mainActivity.SuccessfulAPIRequest(fname, lname, email, id, apiKeyString.toString());
                }
            });

        }
        catch (Exception e) {Log.e(TAG, "error in run", e);}
    }
}
