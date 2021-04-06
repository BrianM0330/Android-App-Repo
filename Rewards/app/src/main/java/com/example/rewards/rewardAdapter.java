package com.example.rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class rewardAdapter extends RecyclerView.Adapter<RewardViewHolder> {
    private static final String TAG = "rewardAdapter";
    private List<Reward> rewardList;
    private ViewProfile profileActivity;

    rewardAdapter(List<Reward> rList, ViewProfile p) {
        this.rewardList = rList;
        this.profileActivity = p;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.reward_recycler_layout, parent, false);

        return new RewardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        try { holder.rewardDate.setText(reward.getDate());}
        catch (Exception e) {e.printStackTrace();}
        holder.rewardNote.setText(reward.getNote());
        holder.rewardGiver.setText(reward.getGiver());
        holder.rewardPoints.setText(reward.getPoints());
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }
}
