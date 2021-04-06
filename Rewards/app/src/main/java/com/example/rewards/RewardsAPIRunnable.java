package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RewardsAPIRunnable implements Runnable{
    public static final String ENDPOINT = "christopherhield.org";

    private GiveRewards givingActivity;

    String recipientUsername;
    String giverUsername;
    String giverName;
    int amount;
    String note;
    String API_KEY;

    RewardsAPIRunnable(GiveRewards act, String rec, String givU, String givN, int am, String n, String API) {
        givingActivity = act;
        this.recipientUsername = rec;
        this.giverUsername = givU;
        this.giverName = givN;
        this.amount = am;
        this.note = n;
        this.API_KEY = API;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        Uri.Builder uri = new Uri.Builder();
        uri
            .scheme("http")
            .authority(ENDPOINT)
            .path("api/Rewards/AddRewardRecord")
            .appendQueryParameter("receiverUser", recipientUsername)
            .appendQueryParameter("giverUser", giverUsername)
            .appendQueryParameter("giverName", giverName)
            .appendQueryParameter("amount", String.valueOf(amount))
            .appendQueryParameter("note", note)
        .build();

        try {
            URL url = new URL(uri.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ApiKey", API_KEY);
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                Log.d("REWARD POST", "Couldn't post the reward" + conn.getResponseMessage());
            }

            InputStream is = conn.getInputStream();
//            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

//            String line;
//            while ((line = reader.readLine()) != null) {
//                responseData.append(line);
//            }
//
//            JSONObject responseJSON = new JSONObject(responseData.toString());

            givingActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    givingActivity.SuccessfulPOST();
                }
            });

        } catch (Exception e) {e.printStackTrace();}


    }
}
