package com.epiccrown.map.minimap.Fragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.ServiceStuff.TrackerJob;
import com.epiccrown.map.minimap.account.LoginActivity;
import com.epiccrown.map.minimap.databaseStuff.DatabaseDataGetter;
import com.epiccrown.map.minimap.databaseStuff.DatabaseOpenHelper;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.helpers.UsefulStaticMethods;

public class SettingsProfile extends Fragment {

    SeekBar numberUsersWidget;
    TextView numberUsersText;
    TextView usernameText;
    TextView groupText;

    ConstraintLayout usernameBox;
    ConstraintLayout groupBox;
    ConstraintLayout alwaysTrackedBox;
    ConstraintLayout deleteBox;

    Switch alwaysTrackedSwitch;
    RadioGroup radioGroup;
    RadioButton radioButton1;
    RadioButton radioButton2;
    RadioButton radioButton3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assignVariables(view);
        assignJob();
        restoreSelectedInterval();
        restoreTextValues();
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreTextValues();
    }

    private void restoreTextValues() {
        usernameText.setText(Preferences.getUsername(getContext()));
        if (!Preferences.getFamily(getContext()).equals(""))
            groupText.setText(Preferences.getFamily(getContext()));
        else
            groupText.setText("-");
    }

    private void restoreSelectedInterval() {
        long interval = Preferences.getTrackingInterval(getContext());
        if (interval == 1000 * 60 * 15)
            radioGroup.check(R.id.radioButton);
        else if (interval == 1000 * 60 * 25)
            radioGroup.check(R.id.radioButton2);
        else if (interval == 1000 * 60 * 35)
            radioGroup.check(R.id.radioButton3);
    }

    private void assignJob() {
        numberOfUsersTab();
        alwaysTrackedTab();
        radioGroupTab();
        usernameTab();
        groupTab();
        deleteTab();
    }

    private void deleteTab() {
        deleteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getResources().getString(R.string.profile_delete_confirm_title))
                        .setMessage(getResources().getString(R.string.profile_delete_confirm_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                new DeleteUser().execute();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    private void groupTab() {
        groupBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Group group = new Group();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_holder, group)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void usernameTab() {
        usernameBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Username username = new Username();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_holder, username)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();

            }
        });
    }

    private void radioGroupTab() {

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                long oldInterval = Preferences.getTrackingInterval(getContext());
                switch (radioButtonID) {
                    case R.id.radioButton:
                        if (oldInterval != 1000 * 60 * 15) {
                            Preferences.setTrackingInterval(getContext(), 1000 * 60 * 15);
                            cancelJob();
                            setJob();
                        }
                        break;
                    case R.id.radioButton2:
                        if (oldInterval != 1000 * 60 * 25) {
                            Preferences.setTrackingInterval(getContext(), 1000 * 60 * 25);
                            cancelJob();
                            setJob();
                        }
                        break;
                    case R.id.radioButton3:
                        if (oldInterval != 1000 * 60 * 35) {
                            Preferences.setTrackingInterval(getContext(), 1000 * 60 * 35);
                            cancelJob();
                            setJob();
                        }
                        break;
                }
            }
        });

    }

    private void alwaysTrackedTab() {
        if (alwaysTrackedBox != null)
            alwaysTrackedBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alwaysTrackedSwitch.setChecked(!alwaysTrackedSwitch.isChecked());
                }
            });
        if (alwaysTrackedSwitch != null) {
            alwaysTrackedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Preferences.setAlwaysTrackedEnabled(getContext(), b);
                    if (b)
                        setJob();
                    else
                        cancelJob();
                }

            });
            alwaysTrackedSwitch.setChecked(Preferences.isAlwaysTracked(getContext()));
        }
    }

    private void numberOfUsersTab() {
        if (numberUsersWidget != null) {
            numberUsersWidget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                private int saves;

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    numberUsersText.setText(i + "");
                    saves = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Preferences.setnumberOfSaves(getContext(), saves);
                    DatabaseOpenHelper openHelper = new DatabaseOpenHelper(getContext());
                    DatabaseDataGetter dataGetter = new DatabaseDataGetter(openHelper);
                    dataGetter.deleteExcessHistory(getContext());
                }
            });
            numberUsersWidget.setProgress(Preferences.numberOfSaves(getContext()));
        }
    }

    public void assignVariables(View view) {
        numberUsersWidget = view.findViewById(R.id.profile_seek_bar);
        numberUsersText = view.findViewById(R.id.profile_number_saves);
        usernameBox = view.findViewById(R.id.profile_username_layout);
        usernameText = view.findViewById(R.id.profile_username_text);
        groupBox = view.findViewById(R.id.profile_group_layout);
        groupText = view.findViewById(R.id.profile_group_text);
        alwaysTrackedBox = view.findViewById(R.id.profile_tracking_switch_layout);
        alwaysTrackedSwitch = view.findViewById(R.id.profile_switch_tracked);
        radioGroup = view.findViewById(R.id.profile_radio_group);
        radioButton1 = view.findViewById(R.id.radioButton);
        radioButton2 = view.findViewById(R.id.radioButton2);
        radioButton3 = view.findViewById(R.id.radioButton3);
        deleteBox = view.findViewById(R.id.profile_delete_account_layout);
    }

    private void setJob() {
        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == TrackerJob.ID) {
                hasBeenScheduled = true;
            }
        }

        if (!hasBeenScheduled) {
            long interval = Preferences.getTrackingInterval(getContext());
            JobInfo jobInfo = new JobInfo.Builder(
                    TrackerJob.ID, new ComponentName(getActivity(), TrackerJob.class))
                    .setPeriodic(interval)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        }
    }

    private void cancelJob() {
        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs())
            if (jobInfo.getId() == TrackerJob.ID)
                scheduler.cancel(TrackerJob.ID);
    }

    private class DeleteUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return new RESTfulHelper().deleteUser(Preferences.getUsername(getContext()));
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                if (s.equals("deleted")) {
                    UsefulStaticMethods.deleteUserAndQuit(getContext());
                } else if (s.equals("error")) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                } else if (s.equals("Nope")) {
                    Toast.makeText(getContext(), "Something gone wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
