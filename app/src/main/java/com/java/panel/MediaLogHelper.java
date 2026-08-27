package com.java.panel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaLogHelper {

    public static String getMediaLogs(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("╭───『 📁 Recent Media Files 』───╮\n\n");

        try {
            Uri uri = MediaStore.Files.getContentUri("external");
            String[] projection = {
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
            };

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    projection,
                    null,
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT 10"
            );

            if (cursor != null) {
                int count = 0;
                while (cursor.moveToNext() && count < 10) {
                    String name = cursor.getString(0);
                    String mime = cursor.getString(1);
                    sb.append("  ➤ ").append(name != null ? name : "Unknown File").append("\n");
                    sb.append("     Type: ").append(mime != null ? mime : "General").append("\n\n");
                    count++;
                }
                cursor.close();
                if (count == 0) {
                    sb.append("  ⚠️ No media files found on the device.\n");
                }
            } else {
                sb.append("  ❌ Could not access the media database.\n");
            }
        } catch (Exception e) {
            sb.append("  ❌ Error: ").append(e.getMessage()).append("\n");
        }

        sb.append("╰────────────────────────╯");
        return sb.toString();
    }
}
