package com.example.rewards;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RewardViewHolder extends RecyclerView.ViewHolder {
    public TextView rewardDate;
    public TextView rewardNote;
    public TextView rewardGiver;
    public TextView rewardPoints;

    public RewardViewHolder(View view) {
        super(view);
        rewardDate = view.findViewById(R.id.rewardDate);
        rewardNote = view.findViewById(R.id.rewardNote);
        rewardGiver = view.findViewById(R.id.rewardGiver);
        rewardPoints = view.findViewById(R.id.rewardPoints);
    }
}
