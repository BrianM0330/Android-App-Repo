package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class initialStockRunnable implements Runnable{

    public static final String TAG = "initialStockRunnable";
    public static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private MainActivity mainActivity;

    initialStockRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Uri datauri = Uri.parse(this.DATA_URL);
        String urlToUse = datauri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect(); //This is the actual connection attempt, returns a response code

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n'); //put all JSON into the sb
            }

            final HashMap<String, String> stockNames = parseResults(sb.toString());
            if (stockNames.size() > 0) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mainActivity, "Successfully loaded initial stocks", Toast.LENGTH_SHORT).show();
                        mainActivity.updateJSONData(stockNames);
                    }
                });
            }
        }
        catch (Exception e) {
            Log.e(TAG, "run: ", e);
        }
    }

    private HashMap<String, String> parseResults(String s) {
        //TODO: NEEDS TO BE A HASHMAP
        HashMap<String, String> stocksToReturn = new HashMap<>();

        try {
            JSONArray jsonArr = new JSONArray(s);

            for (int i=0; i < jsonArr.length(); i++) {
                JSONObject stockObj = (JSONObject) jsonArr.get(i);
                String symbol = stockObj.getString("symbol");
                String companyName = stockObj.getString("name");

                stocksToReturn.put(symbol, companyName);
            }
        }

        catch (Exception e) {
            Log.e(TAG, "parseResults: " + e);
        }
        return stocksToReturn;

    }
}
