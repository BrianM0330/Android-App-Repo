package com.example.stockwatch;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class stockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainActivity;

    stockAdapter(List<Stock> sList, MainActivity m) {
        this.stockList = sList;
        this.mainActivity = m;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW VIEWHOLDER");
        View itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.stocklayout, parent, false);

        itemView.setOnLongClickListener(this.mainActivity);
        itemView.setOnClickListener(this.mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: FILLING VIEW HOLDER STOCK " + position);

        Stock stock = stockList.get(position);

        holder.symbol.setText(stock.getSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.currentPrice.setText(String.valueOf(stock.getPrice()));
        holder.priceDelta.setText(String.valueOf(stock.getDelta()));
        holder.priceDeltaPercent.setText("(" + String.format("%.2f", stock.getDeltaPercentage()) + "%)" );

        /*
        Kept getting issues with using my R.colors.green/red, dug around textView docs and found
        setTextColor. Did some more digging and found Color.
        Just hard coded colors that way.. hope it's okay :(
         */

        if (stock.getDelta() > 0) {
            holder.symbol.setTextColor(Color.parseColor("#007F0E"));
            holder.companyName.setTextColor(Color.parseColor("#007F0E"));
            holder.currentPrice.setTextColor(Color.parseColor("#007F0E"));
            holder.priceDelta.setTextColor(Color.parseColor("#007F0E"));
            holder.priceDeltaPercent.setTextColor(Color.parseColor("#007F0E"));
            holder.tickerArrow.setImageResource(R.drawable.arrowgreen);
        }
        else {
            holder.symbol.setTextColor(Color.parseColor("#FF0000"));
            holder.companyName.setTextColor(Color.parseColor("#FF0000"));
            holder.currentPrice.setTextColor(Color.parseColor("#FF0000"));
            holder.priceDelta.setTextColor(Color.parseColor("#FF0000"));
            holder.priceDeltaPercent.setTextColor(Color.parseColor("#FF0000"));
            holder.tickerArrow.setImageResource(R.drawable.arrowred);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
