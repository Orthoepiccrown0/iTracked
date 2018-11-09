package com.epiccrown.map.minimap.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Username extends Fragment {
    TextInputLayout username;
    FloatingActionButton fb;
    private boolean username_available;
    private boolean save_cliccked;
    private String newUsername;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_username,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        username = view.findViewById(R.id.group_inputLayout);
        username.getEditText().setText(Preferences.getUsername(getContext()));

        fb = view.findViewById(R.id.profile_settings_username_fb);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_cliccked = true;
                new UsernameChanger().execute();
            }
        });

        username.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()>=5){
                    fb.show();
                    newUsername = charSequence.toString().trim();
                    username.setError(getResources().getString(R.string.profile_new_username_hint_checking));
                    username.setErrorTextAppearance(R.style.Username_checking_appearance);
                    new UsernameChanger().execute();
                }else{
                    fb.hide();
                    username.setError(getResources().getString(R.string.profile_new_username_hint_user_exist));
                    username.setErrorTextAppearance(R.style.Username_error_appearance);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
                save_cliccked = false;
                if (newUsername.equals(Preferences.getUsername(getActivity()))) {
                    username.setError(getResources().getString(R.string.profile_new_username_hint_user_is_you));
                    username.setErrorTextAppearance(R.style.Username_okay_appearance);
                    fb.hide();
                } else if (s.equals("User exist")) {
                    username_available = false;
                    fb.hide();
                    username.setError(getResources().getString(R.string.profile_new_username_hint_user_exist));
                    username.setErrorTextAppearance(R.style.Username_error_appearance);
                } else if (s.equals("Username is available")) {
                    username_available = true;
                    fb.show();
                    username.setError(getResources().getString(R.string.profile_new_username_hint_user_available));
                    username.setErrorTextAppearance(R.style.Username_okay_appearance);
                } else if (s.equals("User updated successfully")) {
                    username_available = false;
                    fb.hide();
                    Preferences.setUsername(getContext(), newUsername);
                    username.setError(null);
                }


            }
        }
    }


}
