package com.java.panel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.lang.reflect.Method;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
                "android.intent.action.QUICKBOOT_POWERON".equals(action) || 
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
                
                startServices(context);
            }
        }
    }

    private void startServices(Context context) {
        startServiceSafely(context, ShellService.class);
        startServiceSafely(context, WatchdogService.class);
    }

    private void startServiceSafely(Context context, Class<?> serviceClass) {
        Intent serviceIntent = new Intent(context, serviceClass);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            return;
        } catch (Exception normalEx) {
        }

        try {
            String pkg = context.getPackageName();
            String serviceName = serviceClass.getSimpleName();
            String command = "am start-foreground-service " + pkg + "/." + serviceName;
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                process.waitFor();
                if (process.exitValue() == 0) {
                    return;
                }
            }
        } catch (Exception shizukuEx) {
        }

        try {
            String pkg = context.getPackageName();
            String serviceName = serviceClass.getSimpleName();
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "am start-foreground-service " + pkg + "/." + serviceName});
            process.waitFor();
        } catch (Exception rootEx) {
            rootEx.printStackTrace();
        }
    }
}
