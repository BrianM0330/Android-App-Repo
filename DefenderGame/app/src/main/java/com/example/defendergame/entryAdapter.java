package com.example.defendergame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class entryAdapter extends RecyclerView.Adapter<entryViewHolder> {

    private List<entryItem> entries;
    private GameOver gameActivity;

    entryAdapter(List<entryItem> ldrList, GameOver g) {
        entries = ldrList;
        gameActivity = g;
    }

    @Override
    public entryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_item, parent, false);

        return new entryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(entryViewHolder holder, int position) {
        entryItem entry = entries.get(position);

        holder.recyclerPosition.setText(String.valueOf(entry.getPosition()));
        holder.recyclerInitial.setText(entry.getInitials());
        holder.recyclerLevel.setText(String.valueOf(entry.getLevel()));
        holder.recyclerScore.setText(String.valueOf(entry.getScore()));
        holder.recyclerDateTime.setText(entry.getDateTime());

    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}
