package com.example.mynoteshw2;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Note {
    private String title;
    private String noteContent;
    private String dateCreated;

    Note () {
        this.title = "";
        this.noteContent = "";
        this.dateCreated = new Date().toString();
    }

    Note (String title, String content) {
        this.title = title;
        this.noteContent = content;
        this.dateCreated = new Date().toString();
    }

    public String getTitle() { return this.title; }

    public String getNoteContent() { return this.noteContent; }

    public String getNoteDate() { return this.dateCreated; }

    public Date getFormattedDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return formatter.parse(this.getNoteDate());
    }

    public void setTitle(String title) { this.title = title;}

    public void setNoteContent(String content) { this.noteContent = content;}

    public void setDateCreated(String date) {this.dateCreated = date;}

    @NonNull
    @Override
    public String toString() {
        return this.title + "Created on" + this.dateCreated;
    }

}
