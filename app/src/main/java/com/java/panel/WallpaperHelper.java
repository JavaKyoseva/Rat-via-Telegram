package com.java.panel;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import java.io.File;

public class WallpaperHelper {

    public static String setWallpaper(Context context, String path) {
        return setWallpaper(context, path, -1);
    }

    public static String setWallpaper(Context context, String path, int flag) {
        if (path == null || path.trim().isEmpty()) {
            return "❌ Please specify a valid image file path.\nExample: /wallpaper /storage/emulated/0/panel/wall.jpg";
        }

        File file = new File(path.trim());
        if (!file.exists() || !file.isFile()) {
            return "❌ Image file not found at the specified path: " + path;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap == null) {
                return "❌ Could not decode image file (Unsupported format or corrupted file).";
            }

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            if (wallpaperManager == null) {
                return "❌ WallpaperManager is not available.";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && flag != -1) {
                wallpaperManager.setBitmap(bitmap, null, true, flag);
            } else {
                wallpaperManager.setBitmap(bitmap);
            }

            return "✅ Device wallpaper successfully updated:\n" + file.getAbsolutePath();
        } catch (Exception e) {
            return "❌ Error setting wallpaper: " + e.getMessage();
        }
    }

    public static String executeCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "❌ Usage: /wallpaper <path> or /wallpaper <home|lock|both> <path>";
        }

        command = command.trim();
        String[] parts = command.split("\\s+", 2);
        
        if (parts.length < 1) {
            return "❌ Usage: /wallpaper <path>";
        }

        String subAction = parts[0].toLowerCase();
        if (subAction.equals("home") && parts.length > 1) {
            int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? WallpaperManager.FLAG_SYSTEM : -1;
            return setWallpaper(context, parts[1], flag);
        } else if (subAction.equals("lock") && parts.length > 1) {
            int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? WallpaperManager.FLAG_LOCK : -1;
            return setWallpaper(context, parts[1], flag);
        } else if (subAction.equals("both") && parts.length > 1) {
            int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? (WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK) : -1;
            return setWallpaper(context, parts[1], flag);
        } else {
            return setWallpaper(context, command);
        }
    }
}
