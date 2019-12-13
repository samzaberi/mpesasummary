package com.example.android.mpesasummary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;
    private static final String TAG = "SMS";

    TextView msgText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgText = (TextView) findViewById(R.id.msg_string);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            ArrayList<String> smslist = fetchTexts();
            ArrayList<Map<String, String>> textDetails = extractDetails(smslist);
            ArrayList<Map<String, String>> filterDetails = filter(textDetails);
            ArrayList<MpesaEntry> mpesaEntries = getAmounts(filterDetails);
            Map<Date, List<Double>> datesSent = squash(mpesaEntries);
            ArrayList<MpesaEntry> summedEntries = updateEntries(datesSent);
            ArrayList<MpesaEntry> currentMonth = getCurrentMonth(summedEntries);
            Log.i(TAG, "here");
            msgText.setText(currentMonth.toString());


        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }

    }

    private ArrayList<String> fetchTexts() {
        Uri inboxURI = Uri.parse("content://sms/inbox");
        String[] projection = {Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE};
        String selection = Telephony.Sms.ADDRESS + "= ?";
        String[] selectionArgs = {"MPESA"};
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(inboxURI, projection, selection, selectionArgs, null);

        ArrayList<String> smslist = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));

                smslist.add(body);

            }
            c.close();
        } else {
            Log.e(TAG, "no messages found");
        }
        return smslist;
    }

    private ArrayList<Map<String, String>> extractDetails(ArrayList<String> smslist) {
        ParseUtils parser = new ParseUtils();
        ArrayList<Map<String, String>> textDetails = new ArrayList<>();
        for (String text : smslist) {
            Map<String, String> bodyDetail = parser.parse(text);
            textDetails.add(bodyDetail);
        }
        return textDetails;
    }

    private ArrayList<Map<String, String>> filter(ArrayList<Map<String, String>> textDetails) {
        ArrayList<Map<String, String>> filteredList = new ArrayList<>();
        for (Map<String, String> text : textDetails) {
            if (text.get("date") != "" && text.get("type") != "unknown") {
                filteredList.add(text);
            }
        }
        return filteredList;
    }

    private double convertToDouble(String s) {
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

    private ArrayList<MpesaEntry> getAmounts(ArrayList<Map<String, String>> textDetails) {
        ArrayList<MpesaEntry> mpesaEntries = new ArrayList<>();
        for (Map<String, String> textDetail : textDetails) {
            MpesaEntry mpEntry = new MpesaEntry();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            String datestr = textDetail.get("date");
            Date date = null;
            try {
                date = formatter.parse(datestr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mpEntry.setDate(date);
            double amount = convertToDouble(textDetail.get("amount"));

            if (textDetail.get("type") == "sent") {
                mpEntry.setAmountSent(amount);
                mpEntry.setAmountReceived(0.0);
            } else {
                mpEntry.setAmountSent(0.0);
                mpEntry.setAmountReceived(amount);
            }

            mpesaEntries.add(mpEntry);

        }
        return mpesaEntries;
    }

    private Map<Date, List<Double>> squash(ArrayList<MpesaEntry> entries) {
        Map<Date, List<Double>> grouped = new HashMap<>();

        for (MpesaEntry entry : entries) {
            if (grouped.containsKey(entry.getDate())) {
                List<Double> amounts = grouped.get(entry.getDate());
                double sumSent = amounts.get(0) + entry.getAmountSent();
                double sumReceived = amounts.get(1) + entry.getAmountReceived();
                amounts.clear();
                amounts.add(sumSent);
                amounts.add(sumReceived);
                grouped.put(entry.getDate(), amounts);

            } else {
                List<Double> amounts = new ArrayList<>();
                amounts.add(entry.getAmountSent());
                amounts.add(entry.getAmountReceived());
                grouped.put(entry.getDate(), amounts);
            }
        }
        return grouped;

    }

    private ArrayList<MpesaEntry> updateEntries(Map<Date, List<Double>> amounts) {
        ArrayList<MpesaEntry> newEntries = new ArrayList<>();

        for (Map.Entry<Date, List<Double>> amount : amounts.entrySet()) {
            MpesaEntry entry = new MpesaEntry();
            entry.setDate(amount.getKey());
            entry.setAmountSent(amount.getValue().get(0));
            entry.setAmountReceived(amount.getValue().get(1));
            newEntries.add(entry);
        }
        return newEntries;
    }

    private ArrayList<MpesaEntry> getCurrentMonth(ArrayList<MpesaEntry> entries) {
        LocalDate today=LocalDate.now();

        ArrayList<MpesaEntry> currentMonth = new ArrayList<>();

        for (MpesaEntry entry : entries) {
            if (entry.getDate() == null) continue;
            Date date=entry.getDate();
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.of ( "America/Montreal" );
            ZonedDateTime zdt = ZonedDateTime.ofInstant ( instant , zoneId );
            LocalDate localDate = zdt.toLocalDate();

            if (localDate.getMonth()==today.getMonth()&&localDate.getYear()==today.getYear()){
                currentMonth.add(entry);
            }

        }
        return currentMonth;
    }

}

