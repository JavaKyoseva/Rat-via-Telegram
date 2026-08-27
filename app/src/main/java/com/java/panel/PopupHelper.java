package com.java.panel;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopupHelper {

    public static String showPopup(Context context, String message) {
        try {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager == null) return;

                    int layoutParamType;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        layoutParamType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        layoutParamType = WindowManager.LayoutParams.TYPE_PHONE;
                    }

                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            layoutParamType,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            PixelFormat.TRANSLUCENT
                    );
                    params.gravity = Gravity.CENTER;

                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setBackgroundColor(0xEE1E1E1E);
                    layout.setPadding(50, 50, 50, 50);

                    TextView titleView = new TextView(context);
                    titleView.setText("⚠️ System Warning");
                    titleView.setTextColor(0xFFFF5555);
                    titleView.setTextSize(18);
                    titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                    layout.addView(titleView);

                    TextView msgView = new TextView(context);
                    msgView.setText(message != null ? message : "Unknown system error.");
                    msgView.setTextColor(0xFFFFFFFF);
                    msgView.setTextSize(15);
                    msgView.setPadding(0, 20, 0, 40);
                    layout.addView(msgView);

                    Button btn = new Button(context);
                    btn.setText("Close");
                    layout.addView(btn);

                    windowManager.addView(layout, params);

                    btn.setOnClickListener(v -> {
                        try {
                            windowManager.removeView(layout);
                        } catch (Exception ignored) {}
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return "💬 Fake error/info window displayed on screen.";
        } catch (Exception e) {
            return "❌ Could not show popup (SYSTEM_ALERT_WINDOW permission may be required): " + e.getMessage();
        }
    }
}
