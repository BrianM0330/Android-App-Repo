package com.example.rewards;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileViewHolder extends RecyclerView.ViewHolder {
    public ImageView profileRecyclerPic;
    public TextView profileRecyclerName;
    public TextView profileRecyclerTitle;
    public TextView profileRecyclerDepartment;
    public TextView profileRecyclerPoints;

    public ProfileViewHolder(View view) {
        super(view);
        profileRecyclerPic = view.findViewById(R.id.profileRecyclerPic);
        profileRecyclerName = view.findViewById(R.id.profileRecyclerName);
        profileRecyclerTitle = view.findViewById(R.id.profileRecyclerTitle);
        profileRecyclerDepartment = view.findViewById(R.id.profileRecyclerDepartment);
        profileRecyclerPoints = view.findViewById(R.id.profileRecyclerPoints);
    }
}
