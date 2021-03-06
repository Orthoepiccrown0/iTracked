package com.epiccrown.map.minimap;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {
    private final static String TRACKING_INTERVAL = "tracking_interval";
    private final static String IDcode = "idcode";
    private final static String LOG_CHECK = "lgurs";
    private final static String USERNAME_REQUEST = "usernamereq";
    private final static String FAMILY_KEY = "family_key";
    private final static String ALWAYS_TRACKED = "tracktion";
    private final static String NUMSAVS = "123656598985456";
    private final static String LAST_SCREEN = "lastscreen";

    public static String getLastScreenID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LAST_SCREEN, null);
    }

    public static void setLastScreenID(Context context, String idCode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LAST_SCREEN, idCode)
                .apply();
    }

    public static long getTrackingInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(TRACKING_INTERVAL, 1000 * 60 * 15);
    }

    public static void setTrackingInterval(Context context, long interval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(TRACKING_INTERVAL, interval)
                .apply();
    }

    public static String getIDcode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(IDcode, null);
    }

    public static void setIDcode(Context context, String idCode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(IDcode, idCode)
                .apply();
    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USERNAME_REQUEST, null);
    }

    public static void setUsername(Context context, String username) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USERNAME_REQUEST, username)
                .apply();
    }

    public static boolean isLogged(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOG_CHECK, false);
    }

    public static void setLogged(Context context, boolean isLogged) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOG_CHECK, isLogged)
                .apply();
    }

    public static String getFamily(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(FAMILY_KEY, "");
    }

    public static void setFamily(Context context, String family) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(FAMILY_KEY, family)
                .apply();
    }

    public static boolean isAlwaysTracked(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ALWAYS_TRACKED, true);
    }

    public static void setAlwaysTrackedEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ALWAYS_TRACKED, enabled)
                .apply();
    }

    public static int numberOfSaves(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(NUMSAVS, 25);
    }

    public static void setnumberOfSaves(Context context, int number) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(NUMSAVS, number)
                .apply();
    }


}