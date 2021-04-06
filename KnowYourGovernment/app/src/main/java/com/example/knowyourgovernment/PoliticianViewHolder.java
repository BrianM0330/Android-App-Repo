package com.example.knowyourgovernment;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PoliticianViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView party;
    public TextView officePosition;

    PoliticianViewHolder(View view) {
        super(view);
        this.name = view.findViewById(R.id.recycler_politian_name);
        this.party = view.findViewById(R.id.recycler_politian_party);
        this.officePosition = view.findViewById(R.id.recycler_politian_officePosition);
    }
}
