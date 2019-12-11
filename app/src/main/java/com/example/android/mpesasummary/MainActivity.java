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

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<MpesaEntry> allTextEntries, currentMonthEntries;
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
            allTextEntries = fetchTexts();
            currentMonthEntries = getCurrentMonthEntries(allTextEntries);
            msgText.setText(currentMonthEntries.toString());

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }

    }

    private ArrayList<MpesaEntry> fetchTexts() {
        Uri inboxURI = Uri.parse("content://sms/inbox");
        String[] projection = {Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE};
        String selection = Telephony.Sms.ADDRESS + "= ?";
        String[] selectionArgs = {"MPESA"};
        ArrayList<MpesaEntry> smsList = new ArrayList<>();
        ContentResolver cr = getContentResolver();


        Cursor c = cr.query(inboxURI, projection, selection, selectionArgs, null);
        if (c != null) {
            while (c.moveToNext()) {
                long dateMillis = c.getLong(c.getColumnIndexOrThrow("date"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dateMillis);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                LocalDate date = LocalDate.parse(formattedDate);
                String body = c.getString(c.getColumnIndexOrThrow("body"));


                if (body.contains("sent")) {
                    String amountStr = StringUtils.substringBetween(body, "Ksh", "sent");
                    MpesaEntry entry = new MpesaEntry(date, "sent", amountStr);
                    smsList.add(entry);

                } else if (body.contains("received")) {
                    String amountStr = StringUtils.substringBetween(body, "Ksh", "from");
                    MpesaEntry entry = new MpesaEntry(date, "received", amountStr);
                    smsList.add(entry);

                } else if (body.contains("paid")) {

                    String amountStr = StringUtils.substringBetween(body, "Ksh", "paid");
                    MpesaEntry entry = new MpesaEntry(date, "sent", amountStr);
                    smsList.add(entry);

                } else if (body.contains("bought")) {
                    String amountStr = StringUtils.substringBetween(body, "Ksh", "of");
                    MpesaEntry entry = new MpesaEntry(date, "sent", amountStr);
                    smsList.add(entry);

                } else if (body.contains("give")) {
                    String amountStr = StringUtils.substringBetween(body, "Ksh", "cash");
                    MpesaEntry entry = new MpesaEntry(date, "received", amountStr);
                    smsList.add(entry);

                } else if (body.contains("withdraw")) {
                    String amountStr = StringUtils.substringBetween(body, "Withdraw Ksh", "from");
                    MpesaEntry entry = new MpesaEntry(date, "sent", amountStr);
                    smsList.add(entry);

                }

            }
            c.close();
        } else {
            Log.e(TAG, "no messages found");
        }

        return smsList;

    }

    private ArrayList<MpesaEntry> getCurrentMonthEntries(ArrayList<MpesaEntry> entries) {
        LocalDate today = LocalDate.of(2019, 10, 31);
        ArrayList<MpesaEntry> currentMonth = new ArrayList<>();

        for (MpesaEntry entry : entries) {
            if (entry.getDate().getMonth() == today.getMonth() &&
                    entry.getDate().getYear() == today.getYear()) {
                currentMonth.add(entry);
            }
        }
        return currentMonth;
    }

    private double convertToDouble(String val) {
        String parsedStr = val.contains(",") ? val.replaceAll(",", "") : val;
        return Double.parseDouble(parsedStr);
    }

    private List<MpesaEntry> getSameDayEntries(ArrayList<MpesaEntry> currentMonthEntries, MpesaEntry entry) {
        return currentMonthEntries.subList(currentMonthEntries.indexOf(entry),(currentMonthEntries.lastIndexOf(entry)+1));
    }

    private DailyTransactions findDailyTotals(ArrayList<MpesaEntry> sameDayEntries){
        DailyTransactions dayTransactions = new DailyTransactions(sameDayEntries.get(0).getDate(),0.0,0.0);

        sameDayEntries.forEach(n->{
            if (n.getKeyword()=="sent"){
                double sum=dayTransactions.getAmntSent();
                double amount=convertToDouble(n.getAmount());
                sum+=amount;
                dayTransactions.setAmntSent(sum);
            }else {
                double sum=dayTransactions.getAmntReceived();
                double amount=convertToDouble(n.getAmount());
                sum+=amount;
                dayTransactions.setAmntSent(sum);
            }
        });

        return dayTransactions;
    }

}

