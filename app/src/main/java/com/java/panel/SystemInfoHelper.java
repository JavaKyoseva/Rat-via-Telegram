package com.java.panel;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;

public class SystemInfoHelper {
    public static String getSystemInfo(Context context) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;

            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                activityManager.getMemoryInfo(mi);
            }
            long availableMegs = mi.availMem / 1048576L;
            long totalMegs = mi.totalMem / 1048576L;

            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long totalGb = ((long) stat.getBlockSize() * (long) stat.getBlockCount()) / (1024 * 1024 * 1024);
            long availableGb = ((long) stat.getBlockSize() * (long) stat.getAvailableBlocks()) / (1024 * 1024 * 1024);

            return "📊 Cihaz Raporu:\n" +
                   "🔋 Şarj: %" + level + "\n" +
                   "🧠 RAM: " + (totalMegs - availableMegs) + "MB / " + totalMegs + "MB\n" +
                   "💾 Depolama: " + (totalGb - availableGb) + "GB / " + totalGb + "GB";
        } catch (Exception e) {
            return "❌ Sistem bilgisi alınamadı: " + e.getMessage();
        }
    }
}
