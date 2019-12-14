package com.example.android.mpesasummary;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            ArrayList<String> smslist = fetchTexts();
            ArrayList<Map<String, String>> textDetails = extractDetails(smslist);
            ArrayList<Map<String, String>> filterDetails = filter(textDetails);
            ArrayList<Map<String, String>> convertList = convert(filterDetails);
            Map<String, Map<String, String>> squashList = squash(convertList);
            Map<String, Map<String, String>> currentMonth = getCurrentMonth(squashList);

            Log.i(TAG, "here");
            msgText.setText(currentMonth.toString());

            upload(db,currentMonth,"currentMonth");

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
        Utils parser = new Utils();
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


    private ArrayList<Map<String, String>> convert(ArrayList<Map<String, String>> filteredList) {
        ArrayList<Map<String, String>> convertedList = new ArrayList<>();
        for (Map<String, String> entry : filteredList) {
            String date=entry.get("date");
            String amount=entry.get("amount");
            String type=entry.get("type");
            Map<String, String> cvtEntry = new HashMap<>();
            cvtEntry.put("date", date);
            if (type == "sent") {
                cvtEntry.put("sent", amount);
                cvtEntry.put("received", "0.0");
            } else {
                cvtEntry.put("sent", "0.0");
                cvtEntry.put("received", amount);
            }
            convertedList.add(cvtEntry);
        }
        return convertedList;
    }

    private Map<String, Map<String, String>> squash(ArrayList<Map<String, String>> convertList) {
        Map<String, Map<String, String>> squashed = new HashMap<>();
        Utils utils = new Utils();
        for (Map<String, String> entry : convertList) {
            String date = entry.get("date");
            if (squashed.containsKey(date)) {
                double sent = utils.convertToDouble(squashed.get(date).get("sent"));
                double received = utils.convertToDouble(squashed.get(date).get("received"));
                sent += utils.convertToDouble(entry.get("sent"));
                received += utils.convertToDouble(entry.get("received"));
                Map<String, String> result = new HashMap<>();
                result.put("sent", Double.toString(sent));
                result.put("received", Double.toString(received));
                squashed.put(date, result);

            } else {
                Map<String, String> amounts = new HashMap<>();
                amounts.put("sent", entry.get("sent"));
                amounts.put("received", entry.get("received"));
                squashed.put(date, amounts);
            }
        }
        return squashed;
    }

    private Map<String, Map<String, String>> getCurrentMonth(Map<String, Map<String, String>> entries) {
        Map<String, Map<String, String>> currentMonth = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, Map<String, String>> entry : entries.entrySet()) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            String datestr = entry.getKey();
            Date date = null;
            try {
                date = formatter.parse(datestr);
                Instant instant = date.toInstant();
                ZoneId zoneId = ZoneId.of("America/Montreal");
                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
                LocalDate localDate = zdt.toLocalDate();
                if (localDate.getYear() == today.getYear() &&
                        localDate.getMonth() == today.getMonth()) {
                    currentMonth.put(entry.getKey(), entry.getValue());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return currentMonth;
    }

    private void upload(FirebaseFirestore db, Map<String, Map<String, String>> data, String dataName) {
        db.collection(dataName)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

}

