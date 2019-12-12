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
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    ArrayList<MpesaSmsEntry> allTextEntries, currentMonthEntries;
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

    private ArrayList<MpesaSmsEntry> fetchTexts() {
        Uri inboxURI = Uri.parse("content://sms/inbox");
        String[] projection = {Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE};
        String selection = Telephony.Sms.ADDRESS + "= ?";
        String[] selectionArgs = {"MPESA"};
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(inboxURI, projection, selection, selectionArgs, null);

        ArrayList<MpesaSmsEntry> smsList = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                long dateMillis = c.getLong(c.getColumnIndexOrThrow("date"));
                String body = c.getString(c.getColumnIndexOrThrow("body"));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dateMillis);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                LocalDate date = LocalDate.parse(formattedDate);
                String amountStr="";
                String keyword="";

                if (body.contains("sent")) {
                    keyword = "sent";
                    amountStr = StringUtils.substringBetween(body, "Ksh", "sent");


                } else if (body.contains("received")) {
                    keyword = "received";
                    amountStr = StringUtils.substringBetween(body, "Ksh", "from");


                } else if (body.contains("paid")) {
                    keyword = "sent";
                    amountStr = StringUtils.substringBetween(body, "Ksh", "paid");


                } else if (body.contains("bought")) {
                    keyword = "sent";
                    amountStr = StringUtils.substringBetween(body, "Ksh", "of");


                } else if (body.contains("give")) {
                    keyword = "received";
                    amountStr = StringUtils.substringBetween(body, "Ksh", "cash");


                } else if (body.contains("withdraw")) {
                    keyword = "sent";
                    amountStr = StringUtils.substringBetween(body, "Withdraw Ksh", "from");

                }

                MpesaSmsEntry entry = new MpesaSmsEntry(date, keyword, amountStr);
                smsList.add(entry);

            }
            c.close();
        } else {
            Log.e(TAG, "no messages found");
        }

        return smsList;

    }

    private ArrayList<MpesaSmsEntry> getCurrentMonthEntries(ArrayList<MpesaSmsEntry> entries) {
        LocalDate today = LocalDate.of(2019, 10, 31);
        ArrayList<MpesaSmsEntry> currentMonth = new ArrayList<>();

        for (MpesaSmsEntry entry : entries) {
            if (entry.getDate().getMonth() == today.getMonth() &&
                    entry.getDate().getYear() == today.getYear()) {
                currentMonth.add(entry);
            }
        }
        return currentMonth;
    }

}

