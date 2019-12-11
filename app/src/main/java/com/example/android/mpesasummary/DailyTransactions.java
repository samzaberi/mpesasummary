package com.example.android.mpesasummary;

import androidx.annotation.NonNull;

import java.time.LocalDate;

public class DailyTransactions {
    private LocalDate date;
    private double amntSent;
    private double amntReceived;

    public DailyTransactions(LocalDate date, double amntSent, double amntReceived) {
        this.date = date;
        this.amntSent = amntSent;
        this.amntReceived = amntReceived;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getAmntSent() {
        return amntSent;
    }

    public void setAmntSent(double amntSent) {
        this.amntSent = amntSent;
    }

    public double getAmntReceived() {
        return amntReceived;
    }

    public void setAmntReceived(double amntReceived) {
        this.amntReceived = amntReceived;
    }

    @NonNull
    @Override
    public String toString() {
        return "Date:"+date+",Sent:"+amntSent+",Received:"+amntReceived;
    }
}
