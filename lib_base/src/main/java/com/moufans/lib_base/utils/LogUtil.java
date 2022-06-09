package com.moufans.lib_base.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Describe:日志打印
 */
public class LogUtil {

    public static final String TAG = "LogUtil";
    public static boolean DEBUG = true;

    public static void v(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.v(TAG, message);
        }
    }

    public static void v(String tag, String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.v(tag, message);
        }
    }

    public static void d(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.i(TAG, message);
        }
    }

    public static void i(String tag, String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.i(tag, message);
        }
    }

    public static void w(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.w(TAG, message);
        }

    }

    public static void w(String tag, String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.w(tag, message);
        }

    }

    public static void e(int msg) {
        e(msg + "");
    }


    public static void e(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.e(TAG, message);
        }
    }

    public static void e(String tag, String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.e(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (!TextUtils.isEmpty(message)) {
            if (DEBUG)
                Log.d(TextUtils.isEmpty(tag) ? TAG : tag, message);
        }
    }
}
