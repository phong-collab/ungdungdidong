package com.example.travelapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Helper {
    public static String getAppTransId() {
        return new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    public static String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
    public static boolean isNetworkAvailable(android.content.Context context) {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}