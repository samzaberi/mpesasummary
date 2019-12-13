package com.example.android.mpesasummary;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ParseUtils {
    public Map<String, String> parse(final String message) {
        String[] exploded = message.toLowerCase().split(" ");

        // Get date
        String date = getDate(message.toLowerCase());

        //Get transaction type
        String transactionType = getTransactionType(message.toLowerCase());

        //Get mpesa amount
        String amount = "";

        int moneyCount = 0;
        for (String str : exploded) {
            if (str.startsWith("ksh")) {
                String money = str.replace("ksh", "");
                if (moneyCount == 0) {
                    amount = money;
                }
                moneyCount++;
            }
        }

        Map<String, String> parsed = new HashMap<>();
        parsed.put("date",date);
        parsed.put("type", transactionType.trim());
        parsed.put("amount", amount.trim());
        return parsed;
    }

    private String getTransactionType(String message) {
        String transactionType = "unknown";
        if (message.toLowerCase().contains("you have received")) {
            transactionType = "received";
        } else if (message.toLowerCase().contains("sent to")) {
            transactionType = "sent";
        } else if (message.toLowerCase().contains("withdraw")) {
            transactionType = "sent";
        } else if (message.toLowerCase().contains("paid to")) {
            transactionType = "sent";
        } else if (message.toLowerCase().contains("you bought")) {
            transactionType = "sent";
        } else if (message.toLowerCase().contains("give")) {
            transactionType = "sent";
        }
        return transactionType;
    }

    private String getDate(String message){
        String date = StringUtils.substringBetween(message," on "," at");
        return date;
    }
}
