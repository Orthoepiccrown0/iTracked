package com.epiccrown.map.minimap.account;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.epiccrown.map.minimap.Preferences;
import com.epiccrown.map.minimap.R;
import com.epiccrown.map.minimap.helpers.RESTfulHelper;
import com.epiccrown.map.minimap.helpers.UsefulStaticMethods;
import com.epiccrown.map.minimap.iTrackedActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText input_email;
    EditText input_password;
    Button btn_login;
    TextView link_signup;
    TextView error_message;
    ProgressDialog progressDialog;

    private String Username;
    private String Password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        hideActionBar();
        assignVariables();
        registerLink();
        onLoginClick();
        setUpProgressBar();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
    }

    private void onLoginClick() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp_username = input_email.getText().toString().trim();
                String clear_password = input_password.getText().toString().trim();

                if(checkUsername(tmp_username)&&checkPassword(clear_password)){
                    Username = tmp_username;
                    Password = UsefulStaticMethods.getMD5string(clear_password);
                    new GetUser().execute();
                }
            }
        });
    }

    private void setUpProgressBar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Filling the coffee cup..");
        progressDialog.setTitle("Wait a moment");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    private boolean checkPassword(String clear_password) {
        return(clear_password.length()>4);
    }

    private boolean checkUsername(String tmp_username) {
        return (tmp_username.length()>3);
    }

    private void registerLink() {
        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void assignVariables() {
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        btn_login = findViewById(R.id.btn_login);
        link_signup = findViewById(R.id.link_signup);
        error_message = findViewById(R.id.error_msg_login);
    }

    private void hideActionBar() {
        try {
            getSupportActionBar().hide();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    class GetUser extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            progressDialog.show();
            RESTfulHelper helper = new RESTfulHelper();
            return helper.getUser(Username,Password);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if(s.equals("Nope")){
                error_message.setVisibility(View.VISIBLE);
                error_message.setText(getResources().getText(R.string.login_error_nope));
            }else{
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    String username = jsonObject.getString("username");
                    String idcode = jsonObject.getString("idcode");
                    Preferences.setIDcode(LoginActivity.this,idcode);
                    Preferences.setUsername(LoginActivity.this,username);
                    Preferences.setLogged(LoginActivity.this,true);

                    Intent intent = new Intent(LoginActivity.this, iTrackedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled(String s) {
            Toast.makeText(LoginActivity.this,"An error occurred",Toast.LENGTH_SHORT).show();
        }
    }

}
