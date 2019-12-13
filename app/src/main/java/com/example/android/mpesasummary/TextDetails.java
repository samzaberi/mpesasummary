package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class TextDetails {
    private LocalDate date;
    private double amountSent;
    private double amountReceived;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(double amountSent) {
        this.amountSent = amountSent;
    }

    public double getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(double amountReceived) {
        this.amountReceived = amountReceived;
    }

    @NonNull
    @Override
    public String toString() {
        return date+","+amountReceived+","+amountSent;
    }
}
