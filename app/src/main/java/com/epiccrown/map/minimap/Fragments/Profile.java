package com.epiccrown.map.minimap.Fragments;

import android.graphics.PorterDuff;
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
import android.widget.Switch;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.ServiceStuff.ServicesManager;

public class Profile extends Fragment {
    FloatingActionButton save_btn;
    Switch always_tracked_switch;
    EditText username_input;
    TextInputLayout textLayout;
    DrawerLayout drawer;

    private boolean tracking_changed = false;
    private boolean username_available = false;
    private boolean save_cliccked = false;

    private String newUsername = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        save_btn = v.findViewById(R.id.profile_save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracking_changed) {
                    Preferences.setAlwaysTrackedEnabled(getContext(), always_tracked_switch.isChecked());
                    ServicesManager manager = new ServicesManager(getActivity());
                    if (!always_tracked_switch.isChecked()) {
                        if (manager.isTrackingOn()) {
                            manager.disableTracking();
                        }
                    } else {
                        manager.startTracking();
                    }
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

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_fragment, container, false);

        return v;
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
                if (s.equals("User exist")) {
                    username_available = false;
                    save_btn.hide();

                    textLayout.setHint("Username");
                    textLayout.setError(getResources().getString(R.string.profile_new_username_hint_user_exist));
                } else if (s.equals("Username is available")) {
                    username_available = true;
                    save_btn.show();

                    username_input.getBackground().setColorFilter(getResources().getColor(R.color.confirmcolor), PorterDuff.Mode.SRC_ATOP);
                    textLayout.setError(null);
                } else if (s.equals("User updated successfully")) {
                    username_available = false;
                    save_btn.hide();
                    save_cliccked = false;
                    Preferences.setUsername(getContext(), newUsername);

                    textLayout.setError(null);
                    username_input.getBackground().clearColorFilter();
                }

                if (newUsername.equals(Preferences.getUsername(getActivity()))) {
                    username_input.getBackground().clearColorFilter();
                    textLayout.setError(null);

                }
            }
        }
    }
}
