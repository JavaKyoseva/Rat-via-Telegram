package com.java.panel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CallHelper {

    public static String makeCall(Context context, String phoneNumber) {
        try {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return "❌ Invalid usage: Phone number cannot be empty.";
            }
            
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber.trim()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            return "📞 Call started: " + phoneNumber;
        } catch (SecurityException e) {
            try {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber.trim()));
                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(dialIntent);
                return "⚠️ Direct call permission (CALL_PHONE) not found, forwarded to the dialer screen: " + phoneNumber;
            } catch (Exception ex) {
                return "❌ Call error (Permission required): " + ex.getMessage();
            }
        } catch (Exception e) {
            return "❌ Call error: " + e.getMessage();
        }
    }
}
