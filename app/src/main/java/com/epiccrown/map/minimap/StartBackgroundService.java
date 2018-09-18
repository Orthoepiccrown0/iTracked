package com.epiccrown.map.minimap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.epiccrown.map.minimap.helpers.ServicesManager;

public class StartBackgroundService extends BroadcastReceiver {
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        startTracking();
    }

    private void startTracking() {
        if (Preferences.isAlwaysTracked(mContext)) {
            ServicesManager manager = new ServicesManager(mContext);
            if (!manager.isTrackingOn())
                manager.startTracking();
            else
                Toast.makeText(mContext, "The iTracked service is still on", Toast.LENGTH_LONG).show();
        }
    }

}
