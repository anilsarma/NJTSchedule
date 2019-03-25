package com.smartdeviceny.njts.utils;

import android.content.SharedPreferences;

public class ConfigUtils {

    static public void setupConfigDefaults(SharedPreferences config, String name, String defaultValue) {
        String value = config.getString(name, "");
        if( value.isEmpty()) {
            SharedPreferences.Editor editor  = config.edit();
            editor.putString(name, defaultValue);
            editor.commit();
        }
    }
    static public String getConfig(SharedPreferences config, String name, String defaultValue) {
        return config.getString(name, defaultValue);
    }
    static public int getInt(SharedPreferences config, String name, int defaultValue) {
        return config.getInt(name, defaultValue);
    }

    static public void setConfig(SharedPreferences config, String name, String value) {
        SharedPreferences.Editor editor  = config.edit();
        editor.putString(name, value);
        editor.commit();
    }
    static public void setBoolean(SharedPreferences config, String name, boolean value) {
        SharedPreferences.Editor editor  = config.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }
    static public void setInt(SharedPreferences config, String name, int value) {
        SharedPreferences.Editor editor  = config.edit();
        editor.putInt(name, value);
        editor.commit();
    }
    static public void setLong(SharedPreferences config, String name, long value) {
        SharedPreferences.Editor editor  = config.edit();
        editor.putLong(name, value);
        editor.commit();
    }
}
