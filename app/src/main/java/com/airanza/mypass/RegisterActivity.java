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

import java.sql.SQLException;

public class RegisterActivity extends Activity {

    private LoginDataSource datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.activity_register);

        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);

        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // grab values and try to create the login:

                // Switching to Login Screen/closing register screen
                finish();
            }
        });

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
    }

    public void onRegisterButtonClick(View view) {
        String username = ((EditText) findViewById(R.id.reg_fullname)).getText().toString();
        String password = ((EditText) findViewById(R.id.reg_password)).getText().toString();
        String passwordhint = ((EditText) findViewById(R.id.reg_password_hint)).getText().toString();
        String email = ((EditText) findViewById(R.id.reg_email)).getText().toString();

        Intent result = new Intent("com.airanza.mypass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        // ensure that user enters all three field values:
        if(username.length() <= 0 || password.length() <= 0 || email.length() <= 0) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "User Name, password and email fields must be filled out.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // we cannot have duplicate login:
        if(datasource.isExistingUsername(username)) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "User Name [" + username + "] already exists.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        datasource.createLogin(username, password, passwordhint, email);
        if(!datasource.isValidLogin(username, password)) {
            // display an error message and allow the user to try again or cancel:
            Toast.makeText(getApplicationContext(), "USER NAME [" + username + "] WAS NOT CREATED.  Please try again.", Toast.LENGTH_LONG).show();
            Log.w(this.getClass().getName(), "LOGIN CREATION FAILED FOR [" + username + "] [" + password + "] [" + email + "]");
//            if(getParent() == null) {
//                setResult(Activity.RESULT_CANCELED, result);
//            } else {
//                getParent().setResult(Activity.RESULT_CANCELED, result);
//            }
//            finish();
            return;
        }

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, result);
        } else {
            getParent().setResult(Activity.RESULT_OK, result);
        }
        finish();
    }
}
