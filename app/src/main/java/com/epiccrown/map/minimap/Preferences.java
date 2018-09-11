package com.epiccrown.map.minimap;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {
    private final static String ServiceStarted = "ServiceStarted";
    private final static String IDcode = "idcode";
    private final static String LOG_CHECK = "lgurs";
    private final static String USERNAME_REQUEST = "usernamereq";
    private final static String FAMILY_KEY = "family_key";


    public static boolean getServiceStart(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ServiceStarted, false);
    }
    public static void setServiceStart(Context context, boolean started) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ServiceStarted, started)
                .apply();
    }

    public static String getIDcode(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(IDcode,null);
    }

    public static void setIDcode(Context context, String idCode){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(IDcode, idCode)
                .apply();
    }

    public static String getUsername(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USERNAME_REQUEST,null);
    }

    public static void setUsername(Context context, String username){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USERNAME_REQUEST, username)
                .apply();
    }

    public static boolean isLogged(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOG_CHECK,false);
    }

    public static void setLogged(Context context, boolean isLogged){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOG_CHECK, isLogged)
                .apply();
    }

    public static String getFamily(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(FAMILY_KEY,null);
    }

    public static void setFamily(Context context, String family){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(FAMILY_KEY, family)
                .apply();
    }
}
