package com.example.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {
    public TextView symbol;
    public TextView companyName;
    public TextView currentPrice;
    public TextView priceDelta;
    public TextView priceDeltaPercent;
    public ImageView tickerArrow;

    StockViewHolder(View view) {
        super(view);
        symbol = view.findViewById(R.id.symbol);
        companyName = view.findViewById(R.id.companyName);
        currentPrice = view.findViewById(R.id.currentPrice);
        priceDelta = view.findViewById(R.id.priceDelta);
        priceDeltaPercent = view.findViewById(R.id.priceDeltaPercent);
        tickerArrow = view.findViewById(R.id.tickerArrow);
    }
}
