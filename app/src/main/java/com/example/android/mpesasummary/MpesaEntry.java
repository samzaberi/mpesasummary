package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

public class MpesaEntry {
    private LocalDate date;
    private String keyword;
    private String amount;

    public MpesaEntry(LocalDate date, String keyword, String amount) {
        this.date = date;
        this.keyword = keyword;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
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
        return "date: " + date + ", keyword: " + keyword + ", amount: " + amount;
    }
}
