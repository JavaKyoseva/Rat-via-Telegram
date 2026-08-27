package com.java.panel;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

public class SystemInfoHelper {
    public static String getSystemInfo(Context context) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = -1;
            int temperature = 0;
            int voltage = 0;
            int status = -1;
            String chargeState = "Unknown";

            if (batteryStatus != null) {
                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
                voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                     status == BatteryManager.BATTERY_STATUS_FULL;
                chargeState = isCharging ? "⚡ Charging" : "🔋 Discharging";
                if (status == BatteryManager.BATTERY_STATUS_FULL) chargeState = "🟢 Full";
            }

            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                activityManager.getMemoryInfo(mi);
            }
            long totalRamMb = mi.totalMem / (1024 * 1024);
            long availRamMb = mi.availMem / (1024 * 1024);
            long usedRamMb = totalRamMb - availRamMb;

            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            
            long totalGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024);
            long availGb = (availableBlocks * blockSize) / (1024 * 1024 * 1024);
            long usedGb = totalGb - availGb;

            int cpuCores = Runtime.getRuntime().availableProcessors();
            String abi = (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0) ? Build.SUPPORTED_ABIS[0] : "Unknown";
            String deviceModel = Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;

            return "╭───『 📊 Comprehensive Device Report 』───╮\n\n" +
                   "📱 **Device Profile:**\n" +
                   "  ➤ Model: `" + deviceModel + "`\n" +
                   "  ➤ Android: `" + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")`\n" +
                   "  ➤ Processor: `" + cpuCores + " Cores (" + abi + ")`\n\n" +
                   "🔋 **Battery Status:**\n" +
                   "  ➤ Level: `" + level + "%` (" + chargeState + ")\n" +
                   "  ➤ Temperature: `" + temperature + "°C` | Voltage: `" + voltage + "mV`\n\n" +
                   "🧠 **Memory (RAM):**\n" +
                   "  ➤ Used: `" + usedRamMb + " MB / " + totalRamMb + " MB`\n" +
                   "  ➤ Available: `" + availRamMb + " MB`\n\n" +
                   "💾 **Storage:**\n" +
                   "  ➤ Used: `" + usedGb + " GB / " + totalGb + " GB`\n" +
                   "  ➤ Available: `" + availGb + " GB`\n\n" +
                   "╰─────────────────────────────╯";
        } catch (Exception e) {
            return "❌ Could not retrieve system info: " + e.getMessage();
        }
    }
}
