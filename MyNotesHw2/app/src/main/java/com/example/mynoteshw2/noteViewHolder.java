package com.example.mynoteshw2;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class noteViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView noteContent;
    public TextView dateCreated;

    noteViewHolder (View view) {
        super(view);
        title = view.findViewById(R.id.title);
        noteContent = view.findViewById(R.id.noteContent);
        dateCreated = view.findViewById(R.id.dateCreated);
    }
}
