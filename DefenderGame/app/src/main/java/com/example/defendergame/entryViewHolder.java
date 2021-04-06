package com.example.defendergame;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class entryViewHolder extends RecyclerView.ViewHolder {
    public TextView recyclerPosition;
    public TextView recyclerInitial;
    public TextView recyclerLevel;
    public TextView recyclerScore;
    public TextView recyclerDateTime;

    public entryViewHolder(View view) {
        super(view);

        recyclerPosition = view.findViewById(R.id.recyclerPosition);
        recyclerInitial = view.findViewById(R.id.recyclerInitial);
        recyclerLevel = view.findViewById(R.id.recyclerLevel);
        recyclerScore = view.findViewById(R.id.recyclerScore);
        recyclerDateTime = view.findViewById(R.id.recyclerDateTime);

    }
}
