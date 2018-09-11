package com.epiccrown.map.minimap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

public class StartBackgroundService extends BroadcastReceiver {
    Context mContext;
    private Tracker tracker = null;
    private boolean bound;
    @Override
    public void onReceive(Context context, Intent intent) {
        //if(Preferences.getServiceStart(context)) {
            mContext = context;
            setUpService();
            setUpTrackerThread();
        //}
    }

    private void setUpTrackerThread() {
    }

    private void setUpService() {
        Toast.makeText(mContext,"I AM PREALIVE",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(mContext,Tracker.class);
        mContext.bindService(intent,connection, Context.BIND_AUTO_CREATE);

    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Tracker.TrackerBinder Obinder = (Tracker.TrackerBinder) service;
            tracker = Obinder.getTracker();
            bound = true;
            Toast.makeText(mContext,"I AM ALIVE",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


}
