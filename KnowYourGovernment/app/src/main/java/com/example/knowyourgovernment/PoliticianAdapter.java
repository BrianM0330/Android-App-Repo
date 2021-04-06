package com.example.knowyourgovernment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PoliticianAdapter extends RecyclerView.Adapter<PoliticianViewHolder> {
    private static final String  TAG = "PoliticianAdapter";
    private List<Politician> politicianList;
    private MainActivity mainActivity;

    PoliticianAdapter(List<Politician> pList, MainActivity m) {
        this.politicianList = pList;
        this.mainActivity = m;
    }

    @NonNull
    @Override
    public PoliticianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating politician viewholder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.politician_recycler_layout, parent, false);
        itemView.setOnClickListener(this.mainActivity);
        return new PoliticianViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PoliticianViewHolder holder, int position) {
        Log.d(TAG, "onBind Politician: " + position);

        Politician politician = politicianList.get(position);
        holder.name.setText(politician.getName());
        holder.party.setText(String.format("(%s)", politician.getParty()));
        holder.officePosition.setText(politician.getOfficePosition());
    }

    @Override
    public int getItemCount() {
        return  politicianList.size();
    }
}
