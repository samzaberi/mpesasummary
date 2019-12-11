package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

public class MpesaEntry {
    private String date;
    private String keyword;
    private double amount;

    public MpesaEntry(){
        this.date="";
        this.keyword="";
        this.amount=0.0;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @NonNull
    @Override
    public String toString() {
        return "date: "+date+",keyword: "+keyword+",amount: "+amount;
    }
}
