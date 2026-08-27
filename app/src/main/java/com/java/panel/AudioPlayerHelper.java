package com.java.panel;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.File;

public class AudioPlayerHelper {
    private static MediaPlayer mediaPlayer;

    public static String playAudio(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "❌ Audio file not found: " + filePath;
            }

            stopAudio();

            mediaPlayer = new MediaPlayer();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                );
            }

            mediaPlayer.setDataSource(context, Uri.fromFile(file));
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                try {
                    mp.release();
                } catch (Exception ignored) {}
                mediaPlayer = null;
            });

            return "🎵 Playing audio file in the background:\n📂 " + file.getName();
        } catch (Exception e) {
            return "❌ Playback error: " + e.getMessage();
        }
    }

    public static String stopAudio() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                return "⏹️ Playing audio successfully stopped.";
            }
            return "ℹ️ No active audio currently playing.";
        } catch (Exception e) {
            return "❌ Error stopping audio: " + e.getMessage();
        }
    }
}
