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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
    public static int MAX_LOGIN_ATTEMPTS = 5;
    private int loginAttempts = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    public boolean checkLogin(String username, String password) {
        // TODO: change this to real authentication from sqlite database or datasource:
        if (username.equals("crane.edward@gmail.com") && password.equals("welcome")) {
            return true;
        }
        return false;
    }

    public void onLoginButtonClick(View view) {
        String username = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        Intent result = new Intent("com.airanza.mypass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        // if login is successful, send word to calling Activity that we succeeded:
        if(checkLogin(username, password)) {
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, result);
            } else {
                getParent().setResult(Activity.RESULT_OK, result);
            }
            finish();
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
