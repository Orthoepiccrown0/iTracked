package com.epiccrown.map.minimap;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Tracker extends Service {
    private IBinder binder = new TrackerBinder();
    private Location lastlocation = null;
    private double distanceInMeters = 0;
    public static iTrackedActivity mainact;

    @Override
    public void onCreate() {
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (lastlocation == null) {
                    lastlocation = location;
                    new Sender().execute();
                } else {
                    distanceInMeters += location.distanceTo(lastlocation);
                    if (distanceInMeters > 5) {
                        lastlocation = location;
                        new Sender().execute();
                    }
                    distanceInMeters = 0;
                }


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
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);

        }


    }


    public Tracker() {
    }

    public Location getLastlocation() {
        return lastlocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class TrackerBinder extends Binder {
        Tracker getTracker(){
            return Tracker.this;
        }
    }

    //Sender
    class Sender extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            if(lastlocation!=null&&Preferences.isLogged(getApplicationContext())) {
                RESTfulHelper helper = new RESTfulHelper();
                return helper.sendInfo(lastlocation, getApplication());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("New record created successfully")){
                Toast.makeText(getApplicationContext(),"New record created successfully",Toast.LENGTH_SHORT).show();
            }else if(s.equals("Record updated successfully")){
                Toast.makeText(getApplicationContext(),"Record updated successfully",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),"Something has gone wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        Preferences.setServiceStarted(getApplicationContext(),false);
        Toast.makeText(getApplicationContext(),"Service destroyed",Toast.LENGTH_SHORT).show();
    }
}
