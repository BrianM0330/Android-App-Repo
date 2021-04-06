package com.news;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FetchArticles implements Runnable {

    private MainActivity activity;
    private String API_KEY = "c0b13c5da1dd4e6bbbee3f587cf1e9a1";
    private String ID;

    FetchArticles(MainActivity act, String sourceID) {
        activity = act;
        ID = sourceID;
    }

    @Override
    public void run() {
        StringBuilder responseData = new StringBuilder();
        JSONObject responseObject;
        Uri.Builder uri = new Uri.Builder();
        uri
                .scheme("http")
                .authority("newsapi.org")
                .path("v2/top-headlines")
                .appendQueryParameter("sources", ID)
                .appendQueryParameter("apiKey", API_KEY)
        ;

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
                ArrayList<NewsArticle> toReturn = parser(responseObject);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.parseArticles(toReturn);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<NewsArticle> parser(JSONObject response) {
        ArrayList<NewsArticle> articleList = new ArrayList<>();

        try {
            JSONArray articles = response.getJSONArray("articles");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject articleObject = articles.getJSONObject(i);

                NewsArticle article = new NewsArticle(
                        articleObject.getString("title"),
                        articleObject.getString("publishedAt"),
                        articleObject.getString("author"),
                        articleObject.getString("description"),
                        articleObject.getString("urlToImage"),
                        articleObject.getString("url")
                );

                articleList.add(article);
            }
            return articleList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
