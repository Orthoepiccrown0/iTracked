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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;

public class Family extends Fragment {

    FloatingActionButton save_btn;
    TextView members_count;
    EditText family_input;
    TextInputLayout textLayout;
    Handler count_handler;

    private boolean tracking_changed = false;
    private boolean username_available = false;
    private boolean save_cliccked = false;

    private String newFamily = null;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count_handler = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(Message msg) {
                members_count.setText(msg.arg1+"");
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.family_fragment,container,false);
        save_btn = v.findViewById(R.id.family_save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_cliccked = true;
                save_btn.hide();
                new FamilyChecker().execute();
            }
        });
        save_btn.hide();

        members_count = v.findViewById(R.id.family_count);

        family_input = v.findViewById(R.id.family_name);
        family_input.setText(Preferences.getFamily(getContext()));
        family_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if(s.toString().trim().length()>1){
                    newFamily = s.toString().trim();
                    save_btn.show();
                    save_cliccked=false;
                    new FamilyChecker().execute();
                }
                if(s.toString().trim().equals(Preferences.getFamily(getActivity())))
                    save_btn.hide();

                if(s.toString().trim().length()==0)
                    save_btn.hide();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textLayout = v.findViewById(R.id.family_text_layout);

        setMembers();
        return v;
    }

    private void setMembers() {
        if(Preferences.getFamily(getActivity())!=null){
            newFamily = Preferences.getFamily(getActivity());
            new FamilyChecker().execute();
        }
    }

    private class FamilyChecker extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            RESTfulHelper helper = new RESTfulHelper();
            if(save_cliccked)
                return helper.changeFamily(newFamily,getActivity(),true);

            return helper.changeFamily(newFamily,getContext(),false);
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null){
                if(s.equals("User updated successfully")){
                    Preferences.setFamily(getActivity(),newFamily);
                    return;
                }

                try{
                    final int members = Integer.parseInt(s);
                    final int start = Integer.parseInt(members_count.getText().toString());
                    //сделать handler который меняет количество членов семьи
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            countmembers(start,members);
                        }
                    }).start();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void countmembers(int start, int members){
        if(start<=members){
            for(int i = start;i<members;i++){
                Message msg = Message.obtain();
                msg.arg1 = ++i;
                count_handler.sendMessage(msg);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            for(int i = start;i>members;i--){
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
