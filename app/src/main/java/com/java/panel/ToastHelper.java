package com.java.panel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastHelper {

    public static String showToast(Context context, String message) {
        return showToast(context, message, true);
    }

    public static String showToast(Context context, String message, boolean isLong) {
        try {
            if (message == null || message.trim().isEmpty()) {
                message = "Empty notification message.";
            }
            final String finalMessage = message;
            int duration = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, finalMessage, duration).show();
            });
            return "✅ Toast message displayed on screen: " + finalMessage;
        } catch (Exception e) {
            return "❌ Could not display toast: " + e.getMessage();
        }
    }

    public static String executeCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "❌ Usage: /toast <short|long> <message> or simply provide a message.";
        }

        command = command.trim();
        String[] parts = command.split("\\s+", 2);
        String subAction = parts[0].toLowerCase();

        if (subAction.equals("short")) {
            String msg = parts.length > 1 ? parts[1] : "Notification";
            return showToast(context, msg, false);
        } else if (subAction.equals("long")) {
            String msg = parts.length > 1 ? parts[1] : "Notification";
            return showToast(context, msg, true);
        } else {
            return showToast(context, command, true);
        }
    }
}
