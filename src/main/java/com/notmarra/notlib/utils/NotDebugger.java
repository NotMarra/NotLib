package com.notmarra.notlib.utils;

import java.util.ArrayList;
import java.util.List;

public class NotDebugger {
    static boolean enabled = true;
    static List<String> debugList = new ArrayList<>();

    public static void register(String type) {
        if (debugList.contains(type)) return;
        debugList.add(type);
    }

    public static void unregister(String type) {
        if (!debugList.contains(type)) return;
        debugList.remove(type);
    }

    public static void enable() { enabled = true; }
    public static void disable() { enabled = false; }

    public static boolean isEnabled() { return enabled; }

    public static boolean should(String type) {
        if (!enabled) return false;
        if (debugList.isEmpty()) return true;
        return debugList.contains(type);
    }
}
