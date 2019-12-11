package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

public class MpesaEntry {
    private String date;
    private String keyword;
    private String amount;

    public MpesaEntry(){
        this.date="";
        this.keyword="";
        this.amount="";
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @NonNull
    @Override
    public String toString() {
        return "date: "+date+", keyword: "+keyword+", amount: "+amount;
    }
}
