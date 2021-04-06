package com.example.stockwatch;

public class Stock {
    private String symbol;
    private String companyName;
    private Double currentPrice;
    private Double delta;
    private Double deltaPercentage;

    Stock(String symbol, String companyName){
        this.symbol = symbol;
        this.companyName = companyName;
    }

    public void setPrice(Double priceToSet) { this.currentPrice = priceToSet;}

    public void setDelta(Double deltaToSet) {this.delta = deltaToSet;}

    public void setDeltaPercentage(Double deltaPercToSet) {this.deltaPercentage = deltaPercToSet;}

    String getSymbol() { return this.symbol;}

    String getCompanyName(){return this.companyName;}

    Double getPrice() {return this.currentPrice;}

    Double getDelta() {return this.delta;}

    Double getDeltaPercentage() {
        return this.deltaPercentage;
    }
}
