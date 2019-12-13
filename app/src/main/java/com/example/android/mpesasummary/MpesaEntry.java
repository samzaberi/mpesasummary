package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.Date;

public class MpesaEntry {
    private Date date;
    private double amountSent;
    private double amountReceived;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
