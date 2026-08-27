package com.java.panel;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationHelper {
    private Context context;

    public VibrationHelper(Context context) {
        this.context = context;
    }

    public void triggerVibration(String type, int durationSeconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        long millis = durationSeconds * 1000L;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int amplitude = VibrationEffect.DEFAULT_AMPLITUDE;
            if (type.equalsIgnoreCase("micro")) {
                amplitude = 50;
            } else if (type.equalsIgnoreCase("heavy")) {
                amplitude = 255;
            }
            vibrator.vibrate(VibrationEffect.createOneShot(millis, amplitude));
        } else {
            vibrator.vibrate(millis);
        }
    }
}
