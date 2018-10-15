package com.epiccrown.map.minimap.ServiceStuff;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.helpers.UsefulStaticMethods;

public class TrackerJob extends JobService {
    private Sender sender;
    private Location lastlocation = null;
    private LocationManager manager;

    public static final int ID = 56;
    JobParameters jobParameters;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        this.jobParameters = jobParameters;
        getPostition();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (sender != null)
            sender.cancel(true);
        return false;
    }

    private void getPostition(){
        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastlocation = location;
                if (lastlocation.getAccuracy() < 300) {
                    sender = new Sender();
                    sender.execute(jobParameters);
                    manager.removeUpdates(this);
                    jobFinished(jobParameters, false);
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
                manager.removeUpdates(this);
            }
        };
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);

        }
    }

    private class Sender extends AsyncTask<JobParameters, Void, String> {

        @Override
        protected String doInBackground(JobParameters... voids) {

            if (lastlocation != null && Preferences.isLogged(getApplicationContext())) {
                RESTfulHelper helper = new RESTfulHelper();
                return helper.sendInfo(lastlocation, getApplication());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("New record created successfully")) {
                //Toast.makeText(getApplicationContext(),"New record created successfully",Toast.LENGTH_SHORT).show();
            } else if (s.equals("Record updated successfully")) {
                //Toast.makeText(getApplicationContext(),"Record updated successfully",Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(getApplicationContext(),"Something has gone wrong",Toast.LENGTH_SHORT).show();
            }
        }
    }


}
