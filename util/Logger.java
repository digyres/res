package com.digy.util;

public class Logger {
    private static boolean DEBUG = true;
    public static String LogFormat(String log) {
        return "::::::::::     " + log + "     ::::::::::";

    }

    public static String LogWarnFormat(String log) {
        return "::::::::::     [WARN]" + log + "     ::::::::::";

    }

    public static String LogErrorFormat(String log) {
        return "::::::::::     [ERROR]" + log + "     ::::::::::";

    }

    public static void D(String tag, String log) {
        if(DEBUG) android.util.Log.d(tag, LogFormat(log));
    }

    public static void I(String tag, String log) {
        if(DEBUG) android.util.Log.i(tag, LogFormat(log));
    }

    public static void W(String tag, String log) {
        android.util.Log.w(tag, LogWarnFormat(log));
    }

    public static void E(String tag, String log) {
        android.util.Log.e(tag, LogErrorFormat(log));
    }
}
