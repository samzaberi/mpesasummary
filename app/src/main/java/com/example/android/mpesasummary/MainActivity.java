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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
            ArrayList<String> textlist = fetchTexts();
            ArrayList<Map<String, String>> textDetails = extractDetails(textlist);
            ArrayList<TextDetails> details = getAmounts(textDetails);
            msgText.setText(details.toString());


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

    private ArrayList<Map<String, String>> extractDetails(ArrayList<String> textlist) {
        ParseUtils parser = new ParseUtils();
        ArrayList<Map<String, String>> textDetails = new ArrayList<>();
        for (String text : textlist) {
            Map<String, String> bodyDetail = parser.parse(text);
            textDetails.add(bodyDetail);
        }
        return textDetails;
    }

    private double convertToDouble(String s) {
        if (s != null) {
            String sp = s.replaceAll(",", "");
            return Double.parseDouble(sp);
        } else {
            return 0.0;
        }

    }

    private ArrayList<TextDetails> getAmounts(ArrayList<Map<String, String>> textDetails) {
        ArrayList<TextDetails> detailslist = new ArrayList<>();
        for (Map<String, String> textDetail : textDetails) {
            TextDetails txDetails = new TextDetails();
            LocalDate date = LocalDate.parse(textDetail.get("date"));
            txDetails.setDate(date);
            if (textDetail.get("type") == "sent") {
                double amount = convertToDouble(textDetail.get("amount"));
                txDetails.setAmountSent(amount);
                txDetails.setAmountReceived(0.0);
            } else {
                double amount = convertToDouble(textDetail.get("amount"));
                txDetails.setAmountReceived(amount);
                txDetails.setAmountSent(0.0);
            }
            detailslist.add(txDetails);
        }
        return detailslist;
    }

}

