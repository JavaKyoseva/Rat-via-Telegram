package com.java.panel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileManagerHelper {
    public static String listDirectory(String path) {
        StringBuilder sb = new StringBuilder("📁 Dizin İçeriği [" + path + "]:\n\n");
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                return "❌ Dizin bulunamadı: " + path;
            }
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String type = f.isDirectory() ? "📁 [DIR]" : "📄 [FILE]";
                    sb.append(type).append(" ").append(f.getName()).append("\n");
                }
            } else {
                sb.append("⚠️ Dizin boş veya erişim kısıtlı.");
            }
        } catch (Exception e) {
            return "❌ Dizin listelenemedi: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String findFile(File dir, String name) {
        StringBuilder sb = new StringBuilder("🔍 Arama Sonuçları:\n\n");
        try {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        if (f.getName().toLowerCase().contains(name.toLowerCase())) {
                            sb.append("📁 ").append(f.getAbsolutePath()).append("\n");
                        }
                        // Derinlemesine arama için alt klasörleri de tarayabilir
                    } else {
                        if (f.getName().toLowerCase().contains(name.toLowerCase())) {
                            sb.append("📄 ").append(f.getAbsolutePath()).append("\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "❌ Dosya arama hatası: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String zipFolder(String srcPath, String zipPath) {
        try {
            File srcFile = new File(srcPath);
            if (!srcFile.exists()) return "❌ Sıkıştırılacak kaynak bulunamadı.";

            FileOutputStream fos = new FileOutputStream(zipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            
            if (srcFile.isDirectory()) {
                zipFile(srcFile, srcFile.getName(), zos);
            } else {
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(srcFile);
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            
            zos.close();
            fos.close();
            return "📦 Başarıyla sıkıştırıldı -> " + zipPath;
        } catch (Exception e) {
            return "❌ Sıkıştırma hatası: " + e.getMessage();
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zos) {
        try {
            if (fileToZip.isHidden()) {
                return;
            }
            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zos.putNextEntry(new ZipEntry(fileName));
                    zos.closeEntry();
                } else {
                    zos.putNextEntry(new ZipEntry(fileName + "/"));
                    zos.closeEntry();
                }
                File[] children = fileToZip.listFiles();
                if (children != null) {
                    for (File childFile : children) {
                        zipFile(childFile, fileName + "/" + childFile.getName(), zos);
                    }
                }
                return;
            }
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
