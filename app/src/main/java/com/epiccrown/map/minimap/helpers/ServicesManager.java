package com.epiccrown.map.minimap.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.Tracker;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class ServicesManager {
    private Context mContext;
    private static final long SEND_INTERVAL = 1000*60*10;

    public ServicesManager(Context mContext) {
        this.mContext = mContext;
    }

    public void startTracking() {
        if(isNetworkAvailableAndConnected()) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(mContext, Tracker.class);
            PendingIntent pendingIntent = PendingIntent.getService(mContext,0,intent,0);
            alarmManager.
                    setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),SEND_INTERVAL,pendingIntent);
        }
    }

    public boolean isTrackingOn() {
        Intent i = new Intent(mContext,Tracker.class);
        PendingIntent pi = PendingIntent
                .getService(mContext, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private boolean isNetworkAvailableAndConnected() {

        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    public void disableTracking(){
        if (isTrackingOn()) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(mContext, Tracker.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,0,intent,0);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
