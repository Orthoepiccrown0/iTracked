package com.epiccrown.map.minimap.helpers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.ServiceStuff.TrackerJob;
import com.epiccrown.map.minimap.account.LoginActivity;
import com.epiccrown.map.minimap.databaseStuff.DatabaseDataGetter;
import com.epiccrown.map.minimap.databaseStuff.DatabaseOpenHelper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class UsefulStaticMethods {

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static long getUnixTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static String getMD5string(String plaintext) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(plaintext.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context){
        if(context==null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public static void deleteUserAndQuit(Context context) {
        Preferences.setnumberOfSaves(context, 25);
        Preferences.setFamily(context, "");
        Preferences.setIDcode(context, null);
        Preferences.setLogged(context, false);
        Preferences.setTrackingInterval(context, 1000 * 60 * 15);
        Preferences.setnumberOfSaves(context, 25);
        Preferences.setAlwaysTrackedEnabled(context, true);

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == TrackerJob.ID) {
                scheduler.cancel(TrackerJob.ID);
            }
        }

        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(context);
        DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper);
        dataGetter.deleteHistory();
        dataGetter.deleteFavs();

        Intent intent = new Intent(context,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
