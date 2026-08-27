package com.java.panel;

import android.content.Context;
import java.io.File;

public class ResetHelper {

    public static String resetAll(Context context) {
        try {
            clearDirectory(context.getCacheDir());
            
            if (context.getExternalCacheDir() != null) {
                clearDirectory(context.getExternalCacheDir());
            }
            
            File panelDir = new File("/storage/emulated/0/panel/");
            if (panelDir.exists()) {
                File[] files = panelDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (!f.getName().equals("config.txt")) {
                            deleteRecursive(f);
                        }
                    }
                }
            }
            return "✅ All cache, temporary traces, and panel data have been successfully cleared.";
        } catch (Exception e) {
            return "❌ Error occurred during reset: " + e.getMessage();
        }
    }

    private static void clearDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}
