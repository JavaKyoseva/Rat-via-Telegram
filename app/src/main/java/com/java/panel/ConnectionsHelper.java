package com.java.panel;

import android.content.Context;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.telephony.TelephonyManager;
import android.net.TrafficStats;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;

public class ConnectionsHelper {
    private Context context;

    public ConnectionsHelper(Context context) {
        this.context = context;
    }

    public String execute(String action, String subAction, String param) {
        File panelDir = new File(Environment.getExternalStorageDirectory(), "panel");
        if (!panelDir.exists()) {
            panelDir.mkdirs();
        }

        if (action == null) return "Invalid usage";

        switch (action.toLowerCase()) {
            case "bluetooth":
            case "bt":
                return handleBluetooth(subAction, param, panelDir);
            case "wifi":
                return handleWifi(subAction, param, panelDir);
            case "sim":
                return handleSim(panelDir);
            case "netstats":
                return handleNetStats(panelDir);
            default:
                return "Unknown connections action";
        }
    }

    private String handleBluetooth(String subAction, String param, File dir) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) return "Bluetooth not supported";

        if (subAction == null) return "Bluetooth status: " + btAdapter.isEnabled();

        switch (subAction.toLowerCase()) {
            case "on":
                if (!btAdapter.isEnabled()) {
                    btAdapter.enable();
                }
                return "Bluetooth enabled";
            case "off":
                if (btAdapter.isEnabled()) {
                    btAdapter.disable();
                }
                return "Bluetooth disabled";
            case "scan":
                StringBuilder sb = new StringBuilder("Paired Devices:\n");
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices != null) {
                    for (BluetoothDevice device : pairedDevices) {
                        sb.append(device.getName()).append(" - ").append(device.getAddress()).append("\n");
                    }
                }
                saveToFile(new File(dir, "bluetooth_scan.txt"), sb.toString());
                return "Bluetooth scan completed. Saved to panel folder.";
            default:
                return "Invalid bluetooth action";
        }
    }

    private String handleWifi(String subAction, String param, File dir) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return "Wifi service unavailable";

        if (subAction == null) return "Wifi enabled: " + wifiManager.isWifiEnabled();

        switch (subAction.toLowerCase()) {
            case "on":
                wifiManager.setWifiEnabled(true);
                return "Wifi enabled";
            case "off":
                wifiManager.setWifiEnabled(false);
                return "Wifi disabled";
            case "scan":
                List<ScanResult> results = wifiManager.getScanResults();
                StringBuilder sb = new StringBuilder("Wifi Scan Results:\n");
                if (results != null) {
                    for (ScanResult result : results) {
                        sb.append("SSID: ").append(result.SSID).append(", BSSID: ").append(result.BSSID).append(", Level: ").append(result.level).append("\n");
                    }
                }
                saveToFile(new File(dir, "wifi_scan.txt"), sb.toString());
                return "Wifi scan completed. Saved to panel folder.";
            default:
                return "Invalid wifi action";
        }
    }

    private String handleSim(File dir) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) return "Telephony service unavailable";

        StringBuilder sb = new StringBuilder();
        sb.append("Carrier Name: ").append(tm.getNetworkOperatorName()).append("\n");
        sb.append("Country Iso: ").append(tm.getNetworkCountryIso()).append("\n");
        sb.append("Phone Type: ").append(tm.getPhoneType()).append("\n");
        sb.append("Sim State: ").append(tm.getSimState()).append("\n");
        sb.append("Sim Operator: ").append(tm.getSimOperatorName()).append("\n");

        saveToFile(new File(dir, "sim_info.txt"), sb.toString());
        return "Sim info collected and saved to panel folder.";
    }

    private String handleNetStats(File dir) {
        long rxBytes = TrafficStats.getTotalRxBytes();
        long txBytes = TrafficStats.getTotalTxBytes();
        long mobileRx = TrafficStats.getMobileRxBytes();
        long mobileTx = TrafficStats.getMobileTxBytes();

        StringBuilder sb = new StringBuilder();
        sb.append("Total Rx Bytes: ").append(rxBytes).append("\n");
        sb.append("Total Tx Bytes: ").append(txBytes).append("\n");
        sb.append("Mobile Rx Bytes: ").append(mobileRx).append("\n");
        sb.append("Mobile Tx Bytes: ").append(mobileTx).append("\n");

        saveToFile(new File(dir, "netstats.txt"), sb.toString());
        return "Network stats collected and saved to panel folder.";
    }

    private void saveToFile(File file, String data) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}