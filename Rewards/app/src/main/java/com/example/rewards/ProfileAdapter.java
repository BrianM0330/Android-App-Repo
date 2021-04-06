package com.example.rewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {
    private List<Profile> profiles;
    private Leaderboard leaderboardAct;

    ProfileAdapter(List<Profile> pList, Leaderboard activity) {
        leaderboardAct = activity;
        profiles = pList;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_recycler_layout, parent, false);

        itemView.setOnClickListener(leaderboardAct);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);

        holder.profileRecyclerPoints.setText(profile.getPointsAwarded());
        holder.profileRecyclerName.setText(profile.getFullName());
        holder.profileRecyclerDepartment.setText(profile.getDepartment());
        holder.profileRecyclerTitle.setText(profile.getPosition());
        holder.profileRecyclerPic.setImageBitmap(b64ToImage(profile.getImageBytes()));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    private Bitmap b64ToImage(String bytes) {
        byte[] imageBytes = Base64.decode(bytes, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
