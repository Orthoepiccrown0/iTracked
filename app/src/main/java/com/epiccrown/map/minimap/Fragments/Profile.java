package com.epiccrown.map.minimap.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;

public class Profile extends Fragment {
    FloatingActionButton save_btn;
    Switch always_tracked_switch;
    EditText username_input;
    private boolean tracking_changed = false;
    private boolean username_changed = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_fragment,container,false);
        save_btn = v.findViewById(R.id.profile_save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tracking_changed)
                    Preferences.setAlwaysTrackedEnabled(getContext(),always_tracked_switch.isChecked());
                save_btn.hide();
            }
        });
        save_btn.hide();

        always_tracked_switch = v.findViewById(R.id.switch_always_tracked);
        always_tracked_switch.setChecked(Preferences.isAlwaysTracked(getContext()));
        always_tracked_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked != Preferences.isAlwaysTracked(getContext())) {
                    save_btn.show();
                    tracking_changed = true;
                }else {
                    save_btn.hide();
                    tracking_changed = false;
                }
            }
        });

        username_input = v.findViewById(R.id.profile_username);
        username_input.setText(Preferences.getUsername(getContext()));
        return v;
    }
}
