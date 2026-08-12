package com.java.panel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

public class AppManagerHelper {
    public static String getInstalledApps(Context context) {
        StringBuilder sb = new StringBuilder("📱 Yüklü Uygulamalar:\n\n");
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            for (PackageInfo packageInfo : packages) {
                sb.append("• ").append(packageInfo.packageName).append("\n");
            }
        } catch (Exception e) {
            return "❌ Uygulamalar listelenemedi: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String killApp(String packageName) {
        StringBuilder output = new StringBuilder();
        try {
            String command = "am force-stop " + packageName;
            // Shizuku private access hatasını aşmak için reflection (yansıtma) veya resmi binder process tetiklemesi
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
            }
            return "🛑 Shizuku ile uygulama sonlandırıldı -> " + packageName + "\n" + (output.length() > 0 ? output.toString() : "Başarılı (Çıktı yok)");
        } catch (Exception e) {
            return "❌ Shizuku çalıştırma hatası (Shizuku izni gerekiyor): " + e.getMessage();
        }
    }
}
