package com.airanza.mypass;

/**
 * Created by ecrane on 3/9/2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class LoginActivity extends Activity {
    public static int MAX_LOGIN_ATTEMPTS = 5;
    private int loginAttempts = 0;

    static final int REGISTER_REQUEST = 2;

    private LoginDataSource datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        // if a user is already registered, then don't allow new user.
        if(datasource.logins() > 0) {
            registerScreen.setVisibility(View.INVISIBLE);
        } else {
            // Listening to register new account link
            registerScreen.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // Switching to Register screen
                    Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivityForResult(intent, REGISTER_REQUEST);
                    // process the result in this.onActivityResult();
                }
            });
        }

        String rememberedLastUser = datasource.getRememberedLastUser();
        if(rememberedLastUser.length() > 0) {
            // place rememberedLastUser in the username field
            ((EditText)findViewById(R.id.user_name)).setText(rememberedLastUser);
            ((EditText)findViewById(R.id.password)).requestFocus();
        }
        // check the "Remember Me?" box.
        ((CheckBox)findViewById(R.id.rememberMeCheckBox)).setChecked(rememberedLastUser.length() > 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(getClass().getName(), "inside onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
        if(requestCode == REGISTER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Log.i(getClass().getName(), "REGISTER SUCCEEDED: " + requestCode + " " + resultCode + " " + data);
            } else {
                Log.e(getClass().getName(), "REGISTER FAILED: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
                // Exit this activity, which will also cause progam to shut down:
                finish();
            }
        } else {
            Log.e(getClass().getName(), "ERROR: Received ActivityResult for something we did not request: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
        }
    }

    public boolean checkLogin(String username, String password) {
        return(datasource.isValidLogin(username, password));
    }

    public void onLoginButtonClick(View view) {
        String username = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        boolean isRememberMeChecked = ((CheckBox) findViewById(R.id.rememberMeCheckBox)).isChecked();

        Intent result = new Intent("com.airanza.mypass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        // if login is successful, send word to calling Activity that we succeeded:
        if(checkLogin(username, password)) {
            result.putExtra("logged_in_username", username);
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, result);
            } else {
                getParent().setResult(Activity.RESULT_OK, result);
            }
            datasource.updateRememberedLastUser(username, isRememberMeChecked);

            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Login Failed.  " + ((MAX_LOGIN_ATTEMPTS - loginAttempts) - 1) + " attempts remaining.", Toast.LENGTH_LONG).show();
        }


        // if login failed & we have exceeded allowed attempts, then send signal to calling Activity
        // that login failed:
        if(!checkLogin(username, password) && ++loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                Toast.makeText(getApplicationContext(), "MORE THAN " + MAX_LOGIN_ATTEMPTS + " LOGIN ATTEMPTS.  SHUTTING DOWN", Toast.LENGTH_LONG).show();
                Log.w(this.getClass().getName(), "MORE THAN " + MAX_LOGIN_ATTEMPTS + " LOGIN ATTEMPTS.  SHUTTING DOWN");

                if(getParent() == null) {
                    setResult(Activity.RESULT_CANCELED, result);
                } else {
                    getParent().setResult(Activity.RESULT_CANCELED, result);
                }
                finish();
        }
    }
}
