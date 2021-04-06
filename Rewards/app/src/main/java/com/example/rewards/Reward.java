package com.example.rewards;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Reward {
    private String date;
    private String note;
    private String giver;
    private String points;

    Reward(String date, String note, String giver, String points) {
        this.date = date;
        this.note = note;
        this.giver = giver;
        this.points=points;
    }

    public String getDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat newFormatter = new SimpleDateFormat("MM/dd/yyy", Locale.ENGLISH);
        String realDate = newFormatter.format(formatter.parse(date));
        return realDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGiver() {
        return giver;
    }

    public void setGiver(String giver) {
        this.giver = giver;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
