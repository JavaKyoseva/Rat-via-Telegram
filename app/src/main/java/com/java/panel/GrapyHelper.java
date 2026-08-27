package com.java.panel;

import android.content.Context;

public class GrapyHelper {

    public static String execute(Context context, String param) {
        if (param == null || param.isEmpty()) {
            return "❌ Usage: /grapy <see|touch> [parameters]\nExample:\n  ➤ /grapy see\n  ➤ /grapy touch 500,800";
        }

        String[] parts = param.split("\\s+", 2);
        String action = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        if (action.equals("see")) {
            if (MyAccessibilityService.instance != null) {
                return MyAccessibilityService.instance.getScreenHierarchy();
            } else {
                return "❌ Accessibility Service is not active!";
            }
        } else if (action.equals("touch")) {
            if (MyAccessibilityService.instance != null) {
                try {
                    String[] coords = args.split(",");
                    if (coords.length >= 2) {
                        float x = Float.parseFloat(coords[0].trim());
                        float y = Float.parseFloat(coords[1].trim());
                        boolean success = MyAccessibilityService.instance.clickAtCoordinates(x, y);
                        if (success) {
                            return "👆 Grapy Touch successful: (" + x + ", " + y + ")";
                        } else {
                            return "❌ Grapy Touch failed (Could not click coordinates).";
                        }
                    } else {
                        return "❌ Invalid coordinates. Example: /grapy touch 500,800";
                    }
                } catch (Exception e) {
                    return "❌ Coordinate parse error: " + e.getMessage();
                }
            } else {
                return "❌ Accessibility Service is not active!";
            }
        } else {
            return "❌ Unknown grapy command. Usage: /grapy <see|touch>";
        }
    }
}
