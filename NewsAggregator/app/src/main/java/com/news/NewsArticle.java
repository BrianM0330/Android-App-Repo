package com.news;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class NewsArticle implements Serializable {

    private final String headline;
    private final String date;
    private final String author;
    private final String content;
    private final String imageURL;
    private final String articleURL;

    NewsArticle(String h, String d, String a, String c, String u, String url) {
        headline = h;
        date = d;
        author = a;
        content = c;
        imageURL = u;
        articleURL = url;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat newFormatter = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH);

        String formatted = newFormatter.format(formatter.parse(date));
        if (formatted != null) return formatted;
        else return "";
    }

    public String getAuthor() {
        return author;
    }


    public String getContent() {
        return content;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getArticleURL() {
        return articleURL;
    }
}
