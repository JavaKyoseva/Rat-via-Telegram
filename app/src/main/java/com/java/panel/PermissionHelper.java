package com.java.panel;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static String getPermissions(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("╭───『 🛡️ Permission Status Report 』───╮\n\n");

        String[] permissionsToCheck = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW
        };

        for (String perm : permissionsToCheck) {
            int status = ContextCompat.checkSelfPermission(context, perm);
            String shortName = perm.substring(perm.lastIndexOf('.') + 1);
            if (status == PackageManager.PERMISSION_GRANTED) {
                sb.append("  ✅ ").append(shortName).append(": Granted\n");
            } else {
                sb.append("  ❌ ").append(shortName).append(": Not Granted\n");
            }
        }

        sb.append("\n╰────────────────────────╯");
        return sb.toString();
    }
}
