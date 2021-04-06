package com.example.defendergame;

public class entryItem {

    private int position;
    private String initials;
    private int level;
    private int score;
    private String dateTime;

    entryItem(int p, String i, int l, int s, String dt) {
        position = p;
        initials = i;
        level = l;
        score = s;
        dateTime = dt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
