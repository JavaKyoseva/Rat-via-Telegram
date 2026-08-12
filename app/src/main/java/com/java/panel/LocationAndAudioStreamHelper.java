package com.java.panel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

public class LocationAndAudioStreamHelper {
    public static String getLocationInfo(Context context) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return "❌ Konum servisine erişilemedi.";

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return "❌ Konum izni verilmemiş!";
            }

            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            if (loc != null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();
                return "📍 Konum Raporu:\nEnlem: " + lat + "\nBoylam: " + lon + "\n🔗 Harita Linki: https://www.google.com/maps?q=" + lat + "," + lon;
            }
            return "❌ Son bilinen konum alınamadı.";
        } catch (Exception e) {
            return "❌ Konum hatası: " + e.getMessage();
        }
    }
}
