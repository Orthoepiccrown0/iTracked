package com.epiccrown.map.minimap.ServiceStuff;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Tracker extends IntentService {
    private Location lastlocation = null;
    LocationManager manager;

    public Tracker() {
        super("iTracker service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Toast.makeText(getApplicationContext(),"Service Started",Toast.LENGTH_SHORT).show();
        Log.i("SERVICE iTracked","SERVICE STARTED");
        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastlocation = location;
                new Sender().execute();
                manager.removeUpdates(this);
                //stopSelf();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    //Sender
    class Sender extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            if(lastlocation!=null&& Preferences.isLogged(getApplicationContext())) {
                RESTfulHelper helper = new RESTfulHelper();
                return helper.sendInfo(lastlocation, getApplication());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("New record created successfully")){
                //Toast.makeText(getApplicationContext(),"New record created successfully",Toast.LENGTH_SHORT).show();
            }else if(s.equals("Record updated successfully")){
                //Toast.makeText(getApplicationContext(),"Record updated successfully",Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(getApplicationContext(),"Something has gone wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(getApplicationContext(),"Service Destroyed",Toast.LENGTH_LONG).show();
        Log.i("SERVICE iTracked","SERVICE DESTROYED");
    }
}
