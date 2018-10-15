package com.epiccrown.map.minimap.Fragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.ServiceStuff.TrackerJob;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Profile extends Fragment {
    FloatingActionButton save_btn;
    Switch always_tracked_switch;
    EditText username_input;
    TextInputLayout textLayout;
    DrawerLayout drawer;
    RadioGroup intervalGroup;

    private int radio_btn_checked = 0;
    private boolean tracking_changed = false;
    private boolean username_available = false;
    private boolean save_cliccked = false;
    private String newUsername = null;
    private long newInterval;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        intervalGroup = v.findViewById(R.id.trackingIntervalGroup);
        restoreInterval();
        intervalGroup.setEnabled(Preferences.isAlwaysTracked(getActivity()));
        intervalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.fiftin_min_radiobtn:
                        newInterval = 1000*60*15;
                        break;
                    case R.id.twenty_min_radiobtn:
                        newInterval = 1000*60*20;
                        break;
                    case R.id.twentyfive_min_radiobtn:
                        newInterval = 1000*60*25;
                        break;
                }
                if(radio_btn_checked == i)
                    save_btn.hide();
                else
                    save_btn.show();
            }
        });


        save_btn = v.findViewById(R.id.profile_save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Preferences.getTrackingInterval(getActivity())!=newInterval) {
                    Preferences.setTrackingInterval(getActivity(), newInterval);
                    trackingControlChangeInterval(newInterval);
                }

                if (tracking_changed) {
                    Preferences.setAlwaysTrackedEnabled(getContext(), always_tracked_switch.isChecked());
                    trackingControl(always_tracked_switch.isChecked());
                }
                if (username_available) {
                    save_cliccked = true;
                    new UsernameChanger().execute();
                }
                save_btn.hide();
            }
        });
        save_btn.hide();

        always_tracked_switch = v.findViewById(R.id.switch_always_tracked);
        always_tracked_switch.setChecked(Preferences.isAlwaysTracked(getContext()));
        always_tracked_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != Preferences.isAlwaysTracked(getContext())) {
                    save_btn.show();
                    tracking_changed = true;
                } else {
                    save_btn.hide();
                    tracking_changed = false;
                }

                setEnabledRadioButtons(isChecked);
            }
        });

        username_input = v.findViewById(R.id.profile_username);
        username_input.setText(Preferences.getUsername(getContext()));
        username_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (s.toString().trim().length() > 3) {
                    newUsername = s.toString().trim();
                    new UsernameChanger().execute();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textLayout = v.findViewById(R.id.family_text_layout);
        setEnabledRadioButtons(always_tracked_switch.isChecked());
    }

    private void setEnabledRadioButtons(boolean enabled){
        for(int i = 0; i<intervalGroup.getChildCount();i++){
            intervalGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    private void restoreInterval() {
        newInterval = Preferences.getTrackingInterval(getActivity());
        if(newInterval==1000*60*15) {
            intervalGroup.check(R.id.fiftin_min_radiobtn);
        }else if(newInterval==1000*60*20) {
            intervalGroup.check(R.id.twenty_min_radiobtn);
        }else if(newInterval==1000*60*25) {
            intervalGroup.check(R.id.twentyfive_min_radiobtn);
        }else{
            newInterval = 1000*60*15;
            Preferences.setTrackingInterval(getActivity(),newInterval);
        }
        radio_btn_checked = intervalGroup.getCheckedRadioButtonId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        return v;
    }

    private void trackingControlChangeInterval(long interval) {

        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == TrackerJob.ID) {
                scheduler.cancel(TrackerJob.ID);
            }
        }

        JobInfo jobInfo = new JobInfo.Builder(
                TrackerJob.ID, new ComponentName(getActivity(), TrackerJob.class))
                .setPeriodic(interval)
                .setPersisted(true)
                .build();
        scheduler.schedule(jobInfo);
        Preferences.setTrackingInterval(getActivity(), interval);


    }

    private void trackingControl(boolean isToTrack) {

        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == TrackerJob.ID) {
                hasBeenScheduled = true;
                if (!isToTrack)
                    scheduler.cancel(TrackerJob.ID);
            }
        }
        if (!hasBeenScheduled && isToTrack) {
            JobInfo jobInfo = new JobInfo.Builder(
                    TrackerJob.ID, new ComponentName(getActivity(), TrackerJob.class))
                    .setPeriodic(Preferences.getTrackingInterval(getActivity()))
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        }


    }


    private class UsernameChanger extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            RESTfulHelper helper = new RESTfulHelper();
            if (username_available && save_cliccked)
                return helper.changeUsername(newUsername, getActivity(), true);

            return helper.changeUsername(newUsername, getContext(), false);
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                if (newUsername.equals(Preferences.getUsername(getActivity()))) {
                    textLayout.setError(null);
                    save_btn.hide();
                } else if (s.equals("User exist")) {
                    username_available = false;
                    save_btn.hide();

                    textLayout.setError(getResources().getString(R.string.profile_new_username_hint_user_exist));
                } else if (s.equals("Username is available")) {
                    username_available = true;
                    save_btn.show();

                    textLayout.setError(null);
                } else if (s.equals("User updated successfully")) {
                    username_available = false;
                    save_btn.hide();
                    save_cliccked = false;
                    Preferences.setUsername(getContext(), newUsername);

                    textLayout.setError(null);
                }


            }
        }
    }
}
