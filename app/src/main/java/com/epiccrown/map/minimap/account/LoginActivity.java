package com.epiccrown.map.minimap.account;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    TextInputLayout input_email;
    TextInputLayout input_password;
    Button btn_login;
    TextView link_signup;
    ProgressDialog progressDialog;

    private String Username;
    private String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setBackgroundDrawableResource(R.drawable.login_background_img);
        //hideActionBar();
        assignVariables();
        registerLink();
        onLoginClick();
        setUpProgressBar();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
    }

    private void onLoginClick() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp_username = input_email.getEditText().getText().toString().trim();
                String clear_password = input_password.getEditText().getText().toString().trim();

                if (checkUsername(tmp_username) && checkPassword(clear_password)) {
                    Username = tmp_username;
                    Password = UsefulStaticMethods.getMD5string(clear_password);
                    progressDialog.show();
                    new GetUser().execute();
                    return;
                }
                if(!checkUsername(tmp_username))
                    input_email.setError(getResources().getString(R.string.login_error_put_username));
                else
                    input_email.setError(null);

                if(!checkPassword(clear_password))
                    input_password.setError(getResources().getString(R.string.login_error_put_password));
                else
                    input_password.setError(null);
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
        return (clear_password.length() > 4);
    }

    private boolean checkUsername(String tmp_username) {
        return (tmp_username.length() > 3);
    }

    private void registerLink() {
        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void assignVariables() {
        input_email = findViewById(R.id.username_txtinut);
        input_password = findViewById(R.id.password_txtinut);
        btn_login = findViewById(R.id.btn_login);
        link_signup = findViewById(R.id.link_signup);
    }

    private void hideActionBar() {
        try {
            getSupportActionBar().hide();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showErrorDialog(String title, String errorMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    class GetUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            RESTfulHelper helper = new RESTfulHelper();
            return helper.getUser(Username, Password);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (s.equals("Nope")) {
                showErrorDialog(getResources().getString(R.string.register_error_warning),getResources().getString(R.string.login_error_nope));
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String username = jsonObject.getString("username");
                    String idcode = jsonObject.getString("idcode");
                    Preferences.setIDcode(LoginActivity.this, idcode);
                    Preferences.setUsername(LoginActivity.this, username);
                    Preferences.setLogged(LoginActivity.this, true);

                    Intent intent = new Intent(LoginActivity.this, iTrackedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled(String s) {
            Toast.makeText(LoginActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

}
