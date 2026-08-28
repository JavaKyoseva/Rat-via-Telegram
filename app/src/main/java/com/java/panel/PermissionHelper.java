package com.java.panel;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static String getPermissions(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("╭───『 🛡️ Permission Status Report 』───╮\n\n");

        String[] permissionsToCheck = {
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_SCAN",
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.FOREGROUND_SERVICE,
            "android.permission.FOREGROUND_SERVICE_CAMERA",
            "android.permission.FOREGROUND_SERVICE_MICROPHONE",
            "android.permission.FOREGROUND_SERVICE_LOCATION",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION",
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.EXPAND_STATUS_BAR,
            Manifest.permission.VIBRATE,
            "Shizuku_Permission"
        };

        for (String perm : permissionsToCheck) {
            String shortName = perm.substring(perm.lastIndexOf('.') + 1);
            boolean isGranted = false;

            if (perm.equals("Shizuku_Permission")) {
                shortName = "Shizuku";
                try {
                    Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
                    java.lang.reflect.Method pingMethod = shizukuClass.getDeclaredMethod("pingBinder");
                    boolean binderAlive = (boolean) pingMethod.invoke(null);
                    
                    if (binderAlive) {
                        java.lang.reflect.Method checkMethod = shizukuClass.getDeclaredMethod("checkSelfPermission");
                        int res = (int) checkMethod.invoke(null);
                        isGranted = (res == PackageManager.PERMISSION_GRANTED);
                    }
                } catch (Exception e) {
                    isGranted = false;
                }
            } else if (perm.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    isGranted = Settings.canDrawOverlays(context);
                } else {
                    isGranted = true;
                }
            } else if (perm.equals("android.permission.MANAGE_EXTERNAL_STORAGE")) {
                if (Build.VERSION.SDK_INT >= 30) {
                    try {
                        java.lang.reflect.Method method = Environment.class.getMethod("isExternalStorageManager");
                        isGranted = (Boolean) method.invoke(null);
                    } catch (Exception e) {
                        isGranted = false;
                    }
                } else {
                    isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                }
            } else if (perm.equals(Manifest.permission.WRITE_SETTINGS)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    isGranted = Settings.System.canWrite(context);
                } else {
                    isGranted = true;
                }
            } else if (perm.equals(Manifest.permission.PACKAGE_USAGE_STATS)) {
                try {
                    AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                    int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
                    isGranted = (mode == AppOpsManager.MODE_ALLOWED);
                } catch (Exception e) {
                    isGranted = false;
                }
            } else if (perm.equals(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    isGranted = pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
                } else {
                    isGranted = true;
                }
            } else if (perm.equals(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
                try {
                    String enabledListeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
                    isGranted = enabledListeners != null && enabledListeners.contains(context.getPackageName());
                } catch (Exception e) {
                    isGranted = false;
                }
            } else {
                int status = ContextCompat.checkSelfPermission(context, perm);
                isGranted = (status == PackageManager.PERMISSION_GRANTED);
            }

            if (isGranted) {
                sb.append("  ✅ ").append(shortName).append(": Granted\n");
            } else {
                sb.append("  ❌ ").append(shortName).append(": Not Granted\n");
            }
        }

        sb.append("\n╰────────────────────╯");
        return sb.toString();
    }
}
