package com.epiccrown.map.minimap.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import android.widget.TextView;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Group extends Fragment {
    TextInputLayout group;
    FloatingActionButton fb;
    TextView membersNum;

    private Handler count_handler;
    private boolean save_cliccked = false;
    private String groupName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count_handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                membersNum.setText(msg.arg1 + "");
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_group,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assignVariables(view);
        assignJob();
        if(!Preferences.getFamily(getContext()).equals(""))
            group.getEditText().setText(Preferences.getFamily(getContext()));
    }

    private void assignJob() {
        group.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()>=5||charSequence.toString().trim().length()==0){
                    fb.show();
                    groupName = charSequence.toString().trim();
                    new FamilyChecker().execute();
                }else {
                    fb.hide();
                }
                if(charSequence.toString().trim().equals(Preferences.getFamily(getContext())))
                    fb.hide();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_cliccked = true;
                fb.hide();
                new FamilyChecker().execute();
            }
        });
    }

    private void assignVariables(View view) {
        group = view.findViewById(R.id.group_inputLayout);
        fb = view.findViewById(R.id.profile_settings_group_fb);
        membersNum = view.findViewById(R.id.profile_settings_group_members_num);
    }

    private class FamilyChecker extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            RESTfulHelper helper = new RESTfulHelper();
            if (save_cliccked)
                return helper.changeFamily(groupName, getActivity(), true);

            return helper.changeFamily(groupName, getContext(), false);
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                if (s.equals("User updated successfully")) {
                    Preferences.setFamily(getActivity(), groupName);
                    save_cliccked = false;
                    return;
                }

                try {
                    final int members = Integer.parseInt(s);
                    final int start = Integer.parseInt(membersNum.getText().toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            countmembers(start, members);
                        }
                    }).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void countmembers(int start, int members) {
        if (start <= members) {
            for (int i = start; i < members; i++) {
                Message msg = Message.obtain();
                msg.arg1 = ++i;
                count_handler.sendMessage(msg);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (int i = start; i > members; i--) {
                Message msg = Message.obtain();
                msg.arg1 = --i;
                count_handler.sendMessage(msg);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
