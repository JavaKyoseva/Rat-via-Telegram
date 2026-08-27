package com.java.panel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TtsHelper {
    private static TextToSpeech tts;

    public static String speak(Context context, String text) {
        return speak(context, text, Locale.ENGLISH);
    }

    public static String speak(Context context, String text, Locale locale) {
        try {
            if (text == null || text.trim().isEmpty()) {
                text = "No text provided for speech synthesis.";
            }
            final String finalText = text;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (tts == null) {
                    tts = new TextToSpeech(context, status -> {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(locale);
                            tts.speak(finalText, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    });
                } else {
                    tts.setLanguage(locale);
                    tts.speak(finalText, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });
            return "🗣️ Speech synthesis command processed: " + finalText;
        } catch (Exception e) {
            return "❌ Speech synthesis error: " + e.getMessage();
        }
    }

    public static String executeCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "❌ Usage: /tts <text> or /tts <language_code> <text> (e.g., /tts tr Merhaba)";
        }

        command = command.trim();
        String[] parts = command.split("\\s+", 2);
        
        if (parts[0].length() == 2 && parts.length > 1) {
            String langCode = parts[0].toLowerCase();
            String text = parts[1];
            return speak(context, text, new Locale(langCode));
        } else {
            return speak(context, command, Locale.ENGLISH);
        }
    }

    public static void shutdown() {
        try {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts = null;
            }
        } catch (Exception ignored) {}
    }
}
