package com.java.panel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SensorHelper {
    private Context context;
    private SensorManager sensorManager;

    public SensorHelper(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public String getSensorData(String sensorType) {
        if (sensorManager == null) {
            return "❌ SensorManager not found.";
        }

        sensorType = sensorType.toLowerCase().trim();

        if (sensorType.equals("list") || sensorType.isEmpty()) {
            StringBuilder sb = new StringBuilder("╭───『 📡 Device Sensors 』───╮\n\n");
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            sb.append("Total Sensors: ").append(sensorList.size()).append("\n\n");
            
            for (int i = 0; i < Math.min(sensorList.size(), 25); i++) {
                Sensor s = sensorList.get(i);
                sb.append("  ➤ ").append(s.getName()).append("\n    └ Type: ").append(s.getStringType()).append("\n");
            }
            
            if (sensorList.size() > 25) {
                sb.append("\n... and ").append(sensorList.size() - 25).append(" more sensors.");
            }
            
            sb.append("\n╰────────────────────────╯");
            return sb.toString();
        }

        int sensorTypeConstant = -1;
        String sensorNameDesc = "";

        switch (sensorType) {
            case "proximity":
                sensorTypeConstant = Sensor.TYPE_PROXIMITY;
                sensorNameDesc = "Proximity Sensor";
                break;
            case "light":
                sensorTypeConstant = Sensor.TYPE_LIGHT;
                sensorNameDesc = "Light Sensor";
                break;
            case "accelerometer":
                sensorTypeConstant = Sensor.TYPE_ACCELEROMETER;
                sensorNameDesc = "Accelerometer";
                break;
            case "gyroscope":
                sensorTypeConstant = Sensor.TYPE_GYROSCOPE;
                sensorNameDesc = "Gyroscope";
                break;
            case "magnetic_field":
                sensorTypeConstant = Sensor.TYPE_MAGNETIC_FIELD;
                sensorNameDesc = "Magnetic Field";
                break;
            case "pressure":
                sensorTypeConstant = Sensor.TYPE_PRESSURE;
                sensorNameDesc = "Pressure Sensor";
                break;
            default:
                return "❌ Unknown sensor type.\n\n**Available Parameters:**\n  ➤ `/sensor list`\n  ➤ `/sensor proximity`\n  ➤ `/sensor light`\n  ➤ `/sensor accelerometer`\n  ➤ `/sensor gyroscope`\n  ➤ `/sensor magnetic_field`\n  ➤ `/sensor pressure`";
        }

        Sensor sensor = sensorManager.getDefaultSensor(sensorTypeConstant);
        if (sensor == null) {
            return "❌ " + sensorNameDesc + " not found on the device.";
        }

        final float[] values = new float[3];
        final CountDownLatch latch = new CountDownLatch(1);

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.arraycopy(event.values, 0, values, 0, Math.min(event.values.length, values.length));
                latch.countDown();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        try {
            latch.await(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            sensorManager.unregisterListener(listener);
        }

        StringBuilder result = new StringBuilder();
        result.append("╭───『 📊 ").append(sensorNameDesc).append(" 』───╮\n\n");
        result.append("  ➤ Name: ").append(sensor.getName()).append("\n");
        result.append("  ➤ Vendor: ").append(sensor.getVendor()).append("\n");
        result.append("  ➤ Power Consumption: ").append(sensor.getPower()).append(" mA\n");
        result.append("  ➤ Max Range: ").append(sensor.getMaximumRange()).append("\n\n");
        result.append("  📥 **Current Values:**\n");
        
        for (int i = 0; i < values.length; i++) {
            if (i == 0 && values.length > 1) result.append("    • X / Value 1: ").append(values[i]).append("\n");
            else if (i == 1) result.append("    • Y / Value 2: ").append(values[i]).append("\n");
            else if (i == 2) result.append("    • Z / Value 3: ").append(values[i]).append("\n");
            else result.append("    • Value: ").append(values[i]).append("\n");
        }
        result.append("\n╰────────────────────────╯");

        return result.toString();
    }
}
