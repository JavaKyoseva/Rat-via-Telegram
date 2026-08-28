package com.java.panel;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

public class PopupHelper {

    public static String showPopup(Context context, String param) {
        try {
            if (param == null || param.isEmpty()) {
                return "❌ Usage: /popup <title> , <message> [, <seconds>]\nExample: /popup Warning , Low battery! , 10";
            }

            String title = "Notification";
            String message = param;
            int timeoutSeconds = 0;

            String[] parts = param.split(",");
            if (parts.length >= 2) {
                title = parts[0].trim();
                message = parts[1].trim();
                if (parts.length >= 3) {
                    try {
                        timeoutSeconds = Integer.parseInt(parts[2].trim());
                    } catch (Exception ignored) {}
                }
            }

            final String finalTitle = title;
            final String finalMessage = message;
            final int finalTimeout = timeoutSeconds;

            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(finalTitle);
                    builder.setMessage(finalMessage);
                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    
                    AlertDialog dialog = builder.create();
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    
                    dialog.show();

                    if (finalTimeout > 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            } catch (Exception ignored) {}
                        }, finalTimeout * 1000L);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            String response = "💬 Popup displayed successfully:\n" +
                              "  🔹 Title: `" + title + "`\n" +
                              "  🔹 Message: `" + message + "`";
            if (timeoutSeconds > 0) {
                response += "\n  🔹 Auto-dismiss: `" + timeoutSeconds + "s`";
            }
            return response;
                   
        } catch (Exception e) {
            return "❌ Popup error: " + e.getMessage();
        }
    }
}
