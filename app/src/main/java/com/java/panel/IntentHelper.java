package com.java.panel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentHelper {

    public static String sendIntent(Context context, String action, String data, String pkg) {
        try {
            if (action == null || action.trim().isEmpty()) {
                return "❌ Invalid usage: Intent action cannot be empty.";
            }

            Intent intent = new Intent(action.trim());
            
            if (data != null && !data.trim().isEmpty()) {
                intent.setData(Uri.parse(data.trim()));
            }
            
            if (pkg != null && !pkg.trim().isEmpty()) {
                intent.setPackage(pkg.trim());
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            if (action.contains("BROADCAST") || action.startsWith("com.java.")) {
                context.sendBroadcast(intent);
                return "📡 Broadcast intent successfully sent: " + action;
            } else {
                context.startActivity(intent);
                return "🚀 Activity intent successfully triggered: " + action;
            }
        } catch (Exception e) {
            return "❌ Intent sending error: " + e.getMessage();
        }
    }
}
