package com.java.panel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.app.ActivityManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

public class AppManagerHelper {

    public static String getInstalledApps(Context context) {
        StringBuilder sb = new StringBuilder("📱 Installed Applications:\n\n");
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            for (PackageInfo packageInfo : packages) {
                String appName = "";
                if (packageInfo.applicationInfo != null) {
                    try {
                        appName = packageInfo.applicationInfo.loadLabel(pm).toString() + " - ";
                    } catch (Exception ignored) {}
                }
                sb.append("• ").append(appName).append("`").append(packageInfo.packageName).append("`\n");
            }
        } catch (Exception e) {
            return "❌ Failed to list applications: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String killApp(String packageName) {
        try {
            if (ShellService.instance != null) {
                ActivityManager am = (ActivityManager) ShellService.instance.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    am.killBackgroundProcesses(packageName);
                }
            }
        } catch (Exception ignored) {}

        try {
            String command = "am force-stop " + packageName;
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                if (process.exitValue() == 0) {
                    return "🛑 Application terminated with Shizuku (Force-Stop) -> " + packageName;
                }
            }
        } catch (Exception shizukuEx) {
        }

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "am force-stop " + packageName});
            process.waitFor();
            if (process.exitValue() == 0) {
                return "👑 Application terminated with Root (su) privilege -> " + packageName;
            }
        } catch (Exception rootEx) {
            return "🛑 Normal method (background processes cleared) -> " + packageName + " \n(Note: Shizuku or Root may be required for full force-stop)";
        }

        return "🛑 Application terminated -> " + packageName;
    }
}
