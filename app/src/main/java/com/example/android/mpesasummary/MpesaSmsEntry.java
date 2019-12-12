package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class MpesaSmsEntry {
    private LocalDate date;
    private String keyword;
    private String amount;

    public MpesaSmsEntry(LocalDate date, String keyword, String amount) {
        this.date = date;
        this.keyword = keyword;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getKeyword() {
        return keyword;
    }

    public double getAmount() {
        String ps = amount.replaceAll(",","");
        return Double.parseDouble(ps);
    }


    @NonNull
    @Override
    public String toString() {
        return "Date:"+date+", keyword:"+keyword+", amount:"+amount;
    }
}
