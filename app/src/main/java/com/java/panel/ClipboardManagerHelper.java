package com.java.panel;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardManagerHelper {
    public static String getClipboardText(Context context) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                CharSequence text = item != null ? item.getText() : null;
                return text != null && !text.toString().isEmpty() ? "📋 Pano İçeriği: " + text.toString() : "📋 Pano boş.";
            }
        } catch (Exception e) {
            return "❌ Pano okuma hatası: " + e.getMessage();
        }
        return "📋 Pano boş.";
    }

    public static String setClipboardText(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("remote_text", text);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                return "✅ Pano güncellendi: " + text;
            }
        } catch (Exception e) {
            return "❌ Pano yazma hatası: " + e.getMessage();
        }
        return "❌ Hata: Pano servisine erişilemedi.";
    }
}
