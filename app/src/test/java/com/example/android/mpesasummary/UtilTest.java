package com.example.android.mpesasummary;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UtilTest {
    Utils utils;

    @Before
    public void setUp() throws Exception {
        utils = new Utils();
    }

    @Test
    public void testSentMpesa(){
        String msg = "NEL720UG9V Confirmed. Ksh1,000.00 sent to SAFARICOM POSTPAID BUNDLES for"+
                "account Bundles on 21/5/19 at 1:52 PM New M-PESA balance is Ksh480.00. Transaction cost, Ksh0.0.";
        Map<String,String> response = utils.parse(msg);

        assertEquals("21/5/19",response.get("date"));
        assertEquals("sent",response.get("type"));
        assertEquals("1,000.00",response.get("amount"));


    }

    @Test
    public void testReceivedMpesa(){
        String msg = "NFE9LC9VV Confirmed.You have received Ksh100.00 from SHADRACK DOE 071265109"+
                "on 14/6/19 at 4:07 PM New M-PESA balance is Ksh101.00. To reverse, forward this message to 456.";
        Map<String,String> response = utils.parse(msg);

        assertEquals("14/6/19",response.get("date"));
        assertEquals("received",response.get("type"));
        assertEquals("100.00",response.get("amount"));

    }
}
