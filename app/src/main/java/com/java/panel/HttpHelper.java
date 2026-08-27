package com.java.panel;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class HttpHelper {

    public String execute(String action, String target) {
        File panelDir = new File(Environment.getExternalStorageDirectory(), "panel");
        if (!panelDir.exists()) {
            panelDir.mkdirs();
        }

        if (action == null || target == null) return "Invalid usage";

        switch (action.toLowerCase()) {
            case "get":
                return sendHttpRequest("GET", target, null, panelDir);
            case "post":
                return sendHttpRequest("POST", target, null, panelDir);
            case "ping":
                return sendPing(target, panelDir);
            default:
                return "Unknown http action";
        }
    }

    private String sendHttpRequest(String method, String targetUrl, String postData, File dir) {
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if ("POST".equals(method) && postData != null) {
                conn.setDoOutput(true);
                conn.getOutputStream().write(postData.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
            ));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            String result = "Response Code: " + responseCode + "\n\n" + response.toString();
            File outFile = new File(dir, "http_result.txt");
            FileWriter writer = new FileWriter(outFile);
            writer.write(result);
            writer.flush();
            writer.close();

            return "HTTP " + method + " completed. Result saved to panel folder.";
        } catch (Exception e) {
            return "HTTP Error: " + e.getMessage();
        }
    }

    private String sendPing(String host, File dir) {
        try {
            long startTime = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(host);
            boolean reachable = address.isReachable(3000);
            long duration = System.currentTimeMillis() - startTime;

            StringBuilder sb = new StringBuilder();
            sb.append("Host: ").append(host).append("\n");
            sb.append("IP: ").append(address.getHostAddress()).append("\n");
            sb.append("Reachable: ").append(reachable).append("\n");
            sb.append("Latency: ").append(duration).append(" ms\n");

            File outFile = new File(dir, "ping_result.txt");
            FileWriter writer = new FileWriter(outFile);
            writer.write(sb.toString());
            writer.flush();
            writer.close();

            return "Ping completed. Result saved to panel folder.";
        } catch (Exception e) {
            return "Ping Error: " + e.getMessage();
        }
    }
}
