package com.scorpio.bsjg.utils;

import android.content.Context;

/**
 * Created by 高岳 on 2016/6/30.
 * Describe:SharedPreferences管理类
 */
public class SharedPrefUtil {

    public static final int MODE = Context.MODE_MULTI_PROCESS;
    private static Context mContext;
    public static final String LEJU_PREF_FILE_NAME = "share_pre";

    /**
     * 初始化工具类
     *
     * @param context
     */
    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 删除一个偏好键值对
     *
     * @param key
     */
    public static void remove(String key) {
        if (mContext != null) {
            mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).edit().remove(key).commit();
        }
    }

    /**
     * 添加一个偏好键值
     *
     * @param key
     * @param value  布尔型
     */
    public static void put(String key, boolean value) {
        if (mContext != null) {
            mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).edit().putBoolean(key, value).commit();
        }

    }

    /**
     * 获取偏好设置
     *
     * @param key
     * @param defValue
     * @return :true第一次进入
     */
    public static boolean get(String key, boolean defValue) {
        if (mContext != null) {
            return mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).getBoolean(key, defValue);
        }
        return defValue;
    }
    /**
     * 添加一个偏好键值
     *
     * @param key
     * @param value
     */
    public static void put(String key, String value) {
        if (mContext != null) {
            mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).edit().putString(key, value).commit();
        }

    }

    /**
     * 添加一个偏好键值
     *
     * @param key
     * @param value  int型
     */
    public static void put(String key, int value) {
        if (mContext != null) {
            mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).edit().putInt(key, value).commit();
        }

    }

    /**
     * 获取偏好设置
     *
     * @param key
     * @param defValue
     */
    public static int get(String key, int defValue) {
        if (mContext != null) {
            return mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).getInt(key, defValue);
        }
        return defValue;
    }

    /**
     * 获取偏好设置
     *
     * @param key
     * @param defValue
     * @return
     */
    public static String get(String key, String defValue) {
        if (mContext != null) {
            return mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).getString(key, defValue);
        }
        return defValue;
    }

    /**
     * 添加 Long 类型值
     * @param key
     * @param value
     * @return
     */
    public static void put(String key,long value){
        if (mContext != null) {
            mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).edit().putLong(key, value).commit();
        }
    }

    /**
     * 获取 Long 类型
     * @param key
     * @param defValue
     * @return
     */
    public static long get(String key,long defValue){
        if (mContext != null) {
            return mContext.getSharedPreferences(LEJU_PREF_FILE_NAME, MODE).getLong(key, defValue);
        }
        return defValue;
    }

}
