package com.epiccrown.map.minimap;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.epiccrown.map.minimap.Fragments.Family;
import com.epiccrown.map.minimap.Fragments.Home;
import com.epiccrown.map.minimap.Fragments.Profile;
import com.epiccrown.map.minimap.ServiceStuff.TrackerJob;
import com.epiccrown.map.minimap.account.LoginActivity;

public class iTrackedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    DrawerLayout drawer;
    private TextView username_label;
    private Fragment home;
    private Fragment profile;
    private Fragment family;
    private Fragment fragmentToSet;
    private int err_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLogged();
        setContentView(R.layout.activity_i_tracked);
        setUpDefaultMethods();
        home = new Home();
        showPrimaryFragment(home);
        startTracking();
    }

    private void startTracking() {
        if (err_count < 2) {
            boolean permissions_granted = true;
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions_granted = false;
                err_count++;
                ActivityCompat.requestPermissions(iTrackedActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }

            JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            boolean hasBeenScheduled = false;
            for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == TrackerJob.ID) {
                    hasBeenScheduled = true;
                    if (!permissions_granted)
                        scheduler.cancel(TrackerJob.ID);
                    else if (!isLocationProviderEnabled())
                        scheduler.cancel(TrackerJob.ID);
                }
            }
            if (permissions_granted && isLocationProviderEnabled())
                if (!hasBeenScheduled) {
                    long interval = Preferences.getTrackingInterval(this);
                    JobInfo jobInfo = new JobInfo.Builder(
                            TrackerJob.ID, new ComponentName(this, TrackerJob.class))
                            .setPeriodic(interval)
                            .setPersisted(true)
                            .build();
                    scheduler.schedule(jobInfo);
                }
            if (!isLocationProviderEnabled()) {
                requestLocationAccess();
            }
        }

    }

    private void requestLocationAccess() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Toast.makeText(iTrackedActivity.this, getResources().getString(R.string.location_not_enabled), Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private boolean isLocationProviderEnabled() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void setUpDefaultMethods() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (fragmentToSet != null)
                    showPrimaryFragment(fragmentToSet);
            }


        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_home);

        View headview = navigationView.getHeaderView(0);
        username_label = headview.findViewById(R.id.username_label);
        username_label.setText(Preferences.getUsername(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.i_tracked, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_username) {
            if (profile == null)
                profile = new Profile();
            //showPrimaryFragment(profile);
            fragmentToSet = profile;
            hideKeyboard();
        } else if (id == R.id.menu_family) {
            if (family == null)
                family = new Family();
            //showPrimaryFragment(family);
            fragmentToSet = family;
            hideKeyboard();
        } else if (id == R.id.menu_logout) {
            deleteUser();
        } else if (id == R.id.menu_home) {
            //showPrimaryFragment(home);
            fragmentToSet = home;
            hideKeyboard();
        }
        closeDrawer();
        return true;
    }

    private void hideKeyboard() {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPrimaryFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.main_holder, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .disallowAddToBackStack()
                .commit();
        fragmentToSet = null;
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
    }

    private void deleteUser() {
        Preferences.setLogged(this, false);
        Preferences.setUsername(this, null);
        Preferences.setIDcode(this, null);
        Preferences.setTrackingInterval(this, 1000 * 60 * 15);
        Preferences.setAlwaysTrackedEnabled(this, true);
        Preferences.setFamily(this, null);
        disableJob();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void disableJob() {
        JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == TrackerJob.ID) {
                scheduler.cancel(TrackerJob.ID);
            }
        }
    }

    private void isLogged() {
        if (!Preferences.isLogged(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


}
