package com.example.stockwatch;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class stockFetchRunnable implements Runnable {
    private static final String API_KEY = "pk_4a8961a4536f4248afe9a5f92ce6fe1a";
    private final String urlToFormat = "https://cloud.iexapis.com/stable/stock/%s/quote?token=%s";


    private MainActivity mainActivity;
    private Stock stockToUpdate;

    stockFetchRunnable(MainActivity ma, Stock stockReceived) {
        this.mainActivity = ma;
        this.stockToUpdate = stockReceived;
    }

    @Override
    public void run() {
//  https://cloud.iexapis.com/stable/stock/STOCK_SYMBOL/quote?token=API_KEY
            String symbolForURL = this.stockToUpdate.getSymbol();
            String URLToUse = String.format(urlToFormat, symbolForURL, API_KEY);
            try {
                URL url = new URL(URLToUse);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                StringBuilder sb = new StringBuilder();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);

                JSONObject apiData = new JSONObject(sb.toString());

                Double priceToSet = (apiData.getDouble("latestPrice"));
                Double priceDelta = (apiData.getDouble("change"));
                Double priceDeltaPercent = (apiData.getDouble("changePercent"));

                this.stockToUpdate.setPrice(priceToSet);
                this.stockToUpdate.setDelta(priceDelta);
                this.stockToUpdate.setDeltaPercentage(priceDeltaPercent);

                this.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.updateStockData(stockToUpdate);
                    }
                });
            }
            catch (Exception e) { }
    }
}
