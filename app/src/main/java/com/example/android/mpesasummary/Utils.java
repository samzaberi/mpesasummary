package com.example.android.mpesasummary;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public Map<String, String> parse(final String message) {

        // Get date
        String date = getDate(message.toLowerCase());

        //Get transaction type
        String transactionType = getTransactionType(message.toLowerCase());

        //Get mpesa amount
        String amount = "";

        if (message.contains("sent")) {
            amount = StringUtils.substringBetween(message, "Ksh", "sent");

        } else if (message.contains("received")) {
            amount = StringUtils.substringBetween(message, "Ksh", "from");

        } else if (message.contains("paid")) {
            amount = StringUtils.substringBetween(message, "Ksh", "paid");

        } else if (message.contains("bought")) {
            amount = StringUtils.substringBetween(message, "Ksh", "of");

        } else if (message.contains("Give")) {
            amount = StringUtils.substringBetween(message, "Ksh", "cash");

        } else if (message.contains("Withdraw")) {
            amount = StringUtils.substringBetween(message, "Withdraw Ksh", "from");

        }

        Map<String, String> parsed = new HashMap<>();
        parsed.put("date",date);
        parsed.put("type", transactionType.trim());
        parsed.put("amount", amount);
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
            transactionType = "received";
        }
        return transactionType;
    }

    private String getDate(String message){
        Pattern DATE_PATTERN = Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{2}) at");
        Matcher matcher= DATE_PATTERN.matcher(message);
        while (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    public double convertToDouble(String s) {
        if (s != null && s != "") {
            if (!s.contains(",")) {
                return Double.parseDouble(s);
            } else {
                String sp = s.replaceAll(",", "");
                return Double.parseDouble(sp);
            }
        } else {
            return 0.0;
        }
    }
}
