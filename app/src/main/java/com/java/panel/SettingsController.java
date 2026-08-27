package com.java.panel;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class SettingsController {
    private Context context;
    private CameraManager cameraManager;
    private String cameraId;
    private Handler handler;
    private Runnable sosRunnable;
    private Runnable strobeRunnable;
    private boolean isSosRunning = false;
    private boolean isStrobeRunning = false;

    public SettingsController(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
        try {
            if (cameraManager != null && cameraManager.getCameraIdList().length > 0) {
                this.cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
        }
    }

    public String executeCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length < 2) return "❌ Invalid usage. Example: /settings list or /settings flash on";
        String action = parts[1].toLowerCase();

        switch (action) {
            case "list":
                return "╭───『 ⚙️ Settings List 』───╮\n\n" +
                        "  ➤ /settings flash <on|off|sos|strobe|stop>\n" +
                        "  ➤ /settings rotation <on|off>\n" +
                        "  ➤ /settings airplane <on|off>\n" +
                        "  ➤ /settings sync <on|off>\n" +
                        "  ➤ /settings dnd <on|off>\n" +
                        "  ➤ /settings night <on|off>\n" +
                        "  ➤ /settings bluetooth <on|off>\n" +
                        "  ➤ /settings ringer <normal|silent|vibrate>\n" +
                        "  ➤ /settings location <on|off>\n" +
                        "  ➤ /settings timeout <seconds>\n" +
                        "  ➤ /volume <media|ring|alarm|call> <percent>\n\n" +
                        "╰─────────────────────────────╯";
            case "flash":
                if (parts.length >= 3) {
                    String val = parts[2].toLowerCase();
                    if (val.equals("stop") || val.equals("off")) {
                        stopAllFlash();
                        return "✅ Flashlight and strobe modes stopped / turned off.";
                    }
                    stopAllFlash();
                    if (val.equals("on")) {
                        setFlash(true);
                        return "✅ Flashlight turned on.";
                    } else if (val.equals("sos")) {
                        startSos();
                        return "✅ SOS mode started.";
                    } else if (val.equals("strobe")) {
                        startStrobe();
                        return "✅ Strobe mode started.";
                    }
                } else {
                    stopAllFlash();
                    return "✅ Flashlight turned off.";
                }
                break;
            case "rotation":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setRotation(enable);
                    return "✅ Screen rotation updated.";
                }
                break;
            case "airplane":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setAirplaneMode(enable);
                    return "✅ Airplane mode updated.";
                }
                break;
            case "sync":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setSync(enable);
                    return "✅ Synchronization updated.";
                }
                break;
            case "dnd":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setDnd(enable);
                    return "✅ Do Not Disturb updated.";
                }
                break;
            case "night":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setNightMode(enable);
                    return "✅ Night mode updated.";
                }
                break;
            case "bluetooth":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setBluetooth(enable);
                    return "✅ Bluetooth updated.";
                }
                break;
            case "ringer":
                if (parts.length >= 3) {
                    setRingerMode(parts[2].toLowerCase());
                    return "✅ Ringer mode updated (" + parts[2] + ").";
                }
                break;
            case "location":
                if (parts.length >= 3) {
                    boolean enable = parts[2].toLowerCase().equals("on");
                    setLocationMode(enable);
                    return "✅ Location (GPS) updated.";
                }
                break;
            case "timeout":
                if (parts.length >= 3) {
                    try {
                        int seconds = Integer.parseInt(parts[2]);
                        setScreenTimeout(seconds);
                        return "✅ Screen timeout set: " + seconds + " seconds.";
                    } catch (NumberFormatException e) {
                        return "❌ Invalid duration value.";
                    }
                }
                break;
        }
        return "❌ Invalid settings command or missing parameters. For available commands: /settings list";
    }

    private String executeShizukuOrShell(String command) {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                process.waitFor();
                return "SUCCESS";
            }
        } catch (Exception e) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
                process.waitFor();
                return "SUCCESS";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "FAIL";
    }

    public String setVolume(String type, int percent) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return "❌ Could not access audio manager.";
        
        int streamType = AudioManager.STREAM_MUSIC;
        if (type.equals("media")) {
            streamType = AudioManager.STREAM_MUSIC;
        } else if (type.equals("ring")) {
            streamType = AudioManager.STREAM_RING;
            try {
                if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || 
                    am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            } catch (Exception ignored) {}
        } else if (type.equals("alarm")) {
            streamType = AudioManager.STREAM_ALARM;
        } else if (type.equals("call")) {
            streamType = AudioManager.STREAM_VOICE_CALL;
        } else {
            return "❌ Invalid audio type. Available: media, ring, alarm, call";
        }

        try {
            int max = am.getStreamMaxVolume(streamType);
            int target = (percent * max) / 100;
            am.setStreamVolume(streamType, target, 0);
            return "✅ Volume level set (" + type + "): " + percent + "%";
        } catch (Exception e) {
            return "❌ Volume change error: " + e.getMessage();
        }
    }

    private void setFlash(boolean enable) {
        if (cameraId == null || cameraManager == null) return;
        try {
            cameraManager.setTorchMode(cameraId, enable);
        } catch (CameraAccessException e) {
        }
    }

    private void stopAllFlash() {
        isSosRunning = false;
        isStrobeRunning = false;
        if (sosRunnable != null) {
            handler.removeCallbacks(sosRunnable);
        }
        if (strobeRunnable != null) {
            handler.removeCallbacks(strobeRunnable);
        }
        setFlash(false);
    }

    private void startSos() {
        if (isSosRunning || cameraId == null || cameraManager == null) return;
        isSosRunning = true;
        sosRunnable = new Runnable() {
            private boolean state = false;
            private int step = 0;

            @Override
            public void run() {
                if (!isSosRunning) return;
                try {
                    state = !state;
                    cameraManager.setTorchMode(cameraId, state);
                    long delay = 200;
                    step++;
                    if (step % 6 == 0) {
                        delay = 600;
                    }
                    handler.postDelayed(this, delay);
                } catch (CameraAccessException e) {
                }
            }
        };
        handler.post(sosRunnable);
    }

    private void startStrobe() {
        if (isStrobeRunning || cameraId == null || cameraManager == null) return;
        isStrobeRunning = true;
        strobeRunnable = new Runnable() {
            private boolean state = false;

            @Override
            public void run() {
                if (!isStrobeRunning) return;
                try {
                    state = !state;
                    cameraManager.setTorchMode(cameraId, state);
                    handler.postDelayed(this, 100);
                } catch (CameraAccessException e) {
                }
            }
        };
        handler.post(strobeRunnable);
    }

    private void setRotation(boolean enable) {
        int val = enable ? 1 : 0;
        executeShizukuOrShell("settings put system accelerometer_rotation " + val);
    }

    private void setAirplaneMode(boolean enable) {
        int val = enable ? 1 : 0;
        executeShizukuOrShell("settings put global airplane_mode_on " + val);
        executeShizukuOrShell("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + enable);
    }

    private void setSync(boolean enable) {
        try {
            ContentResolver.setMasterSyncAutomatically(enable);
        } catch (Exception e) {
            executeShizukuOrShell("sync " + (enable ? "on" : "off"));
        }
    }

    private void setDnd(boolean enable) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                if (enable) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                }
            } else {
                String pkg = context.getPackageName();
                executeShizukuOrShell("cmd notification set_dnd " + (enable ? "none" : "all"));
            }
        }
    }

    private void setNightMode(boolean enable) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            if (enable) {
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
            } else {
                uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setBluetooth(boolean enable) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (enable) {
                    bluetoothAdapter.enable();
                } else {
                    bluetoothAdapter.disable();
                }
            }
        } catch (Exception e) {
            executeShizukuOrShell("service call bluetooth_manager 6");
        }
    }

    private void setRingerMode(String mode) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        try {
            if (mode.equals("normal")) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else if (mode.equals("silent")) {
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if (mode.equals("vibrate")) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        } catch (Exception e) {
        }
    }

    private void setLocationMode(boolean enable) {
        int mode = enable ? 3 : 0;
        executeShizukuOrShell("settings put secure location_mode " + mode);
    }

    private void setScreenTimeout(int seconds) {
        executeShizukuOrShell("settings put system screen_off_timeout " + (seconds * 1000));
    }
}
