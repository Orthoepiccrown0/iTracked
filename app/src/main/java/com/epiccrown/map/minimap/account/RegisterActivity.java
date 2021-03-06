package com.epiccrown.map.minimap.account;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    EditText input_username;
    EditText input_password;
    EditText input_password_repeat;
    Button register_btn;

    ProgressDialog progressDialog;

    private String idcode;
    private String Username;
    private String Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setBackgroundDrawableResource(R.drawable.register_background_img);
        setTitle("Register");
        assignVariables();
        //hideActionBar();
        onRegisterClick();
        setUpProgressBar();
    }

    private void hideActionBar() {
        try {
            getSupportActionBar().hide();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setUpProgressBar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Filling the coffee cup..");
        progressDialog.setTitle("Wait a moment");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    private void onRegisterClick() {
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp_username = input_username.getText().toString().trim();
                String tmp_pass = input_password.getText().toString().trim();
                String tmp_pass_repeat = input_password_repeat.getText().toString().trim();

                if (checkUsername(tmp_username) && checkPasswords(tmp_pass, tmp_pass_repeat)) {
                    progressDialog.show();
                    new SendUser().execute();
                }
            }
        });
    }

    private void assignVariables() {
        input_username = findViewById(R.id.input_username_register);
        input_password = findViewById(R.id.input_password_register);
        input_password_repeat = findViewById(R.id.input_password_repeat_register);
        register_btn = findViewById(R.id.btn_register);
        //error_message = findViewById(R.id.error_msg_register);
    }

    private boolean checkUsername(String username) {
        if (username.length() == 0) {
            showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_pick_username));
            return false;
        }

        if (!(username.length() > 3)) {
            showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_short_username));
            return false;
        }

        if(username.contains(" ")){
            showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_short_username));
            return false;
        }

        Username = username;
        return true;
    }

    private boolean checkPasswords(String pass1, String pass2) {
        if (pass1.length() > 4) {
            if (pass1.equals(pass2)) {
                Password = UsefulStaticMethods.getMD5string(pass1);
                idcode = UsefulStaticMethods.getMD5string(Username + Password);
                Preferences.setIDcode(getApplicationContext(), idcode);
                return true;
            } else {
                showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_different_pass));
                return false;
            }
        } else {
            showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_short_password));
            return false;
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

    class SendUser extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            return new RESTfulHelper().sendUser(Username, Password, idcode, getApplicationContext());
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (s.trim().equals("User exist")) {
                showErrorDialog(getResources().getString(R.string.register_error_warning), getResources().getString(R.string.register_error_user_exist));
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String username = jsonObject.getString("username");
                    String idcode = jsonObject.getString("idcode");
                    Preferences.setIDcode(RegisterActivity.this, idcode);
                    Preferences.setUsername(RegisterActivity.this, username);
                    Preferences.setLogged(RegisterActivity.this, true);

                    Intent intent = new Intent(RegisterActivity.this, iTrackedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Intent intent = new Intent(RegisterActivity.this, iTrackedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

        @Override
        protected void onCancelled(String s) {
            Toast.makeText(RegisterActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }

}
