/*
 * Copyright (c) 2015.
 *
 * AIRANZA, INC.
 * _____________
 *   [2015] - [${YEAR}] Adobe Systems Incorporated
 *   All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Airanza, Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Airanza Inc.
 *  and its suppliers and may be covered by U.S. and Foreign Patents,
 *  patents in process, and are protected by trade secret or copyright law
 *
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Airanza Inc.
 */

package com.airanza.apass;

/**
 * Created by ecrane on 3/9/2015.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.sql.SQLException;

public class LoginActivity extends ActionBarActivity {
    static final int LOGIN_REQUEST = 1;
    static final int REGISTER_REQUEST = 2;
    static final int CHANGE_LOGIN = 3;

    private LoginDataSource datasource;

    // for saving login state information across Activity lifecycle as needed.
    // there will be a grace period of STAY_LOGGED_IN_MINUTES whereby a user
    // can navigate away from the screen and return if they have logged in during that
    // grace period without entering their username and password again.
    private SharedPreferences prefs = null;
    static final String LOGGED_IN = "logged_in";
    static final String LOGGED_IN_USER = "logged_in_user";
    static final String LOGGED_IN_PASSWORD = "logged_in_password";
    static final String LOGGED_IN_TIME = "logged_in_time";

    static final String USER_EMAIL_ADDRESS = "user_email_address";
    static final String REGISTER_ACTION = "register_action";

    private boolean logged_in = false;
    private String logged_in_user = "";
    private long logged_in_time = 0;
    private final int STAY_LOGGED_IN_MINUTES = 2;

    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        // Google Analytics Logger:
        APassApplication application = (APassApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(this.getLocalClassName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        findViewById(R.id.show_password_hint).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onShowPasswordHint();
            }
        });

        // if there are no users registered, then open the RegisterActivity:
        if(datasource.logins() == 0) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST);
        }

        String rememberedLastUser = datasource.getRememberedLastUser();
        if(rememberedLastUser.length() > 0) {
            // place rememberedLastUser in the username field
            ((EditText)findViewById(R.id.user_name)).setText(rememberedLastUser);
            (findViewById(R.id.password)).requestFocus();
        }
        // check the "Remember Me?" box.
        ((CheckBox)findViewById(R.id.rememberMeCheckBox)).setChecked(rememberedLastUser.length() > 0);

        addTextChangedListener();
        // hide keyboard, no matter what field has focus:
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow((findViewById(R.id.password)).getWindowToken(), 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(getClass().getName(), "inside onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
        if(requestCode == REGISTER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Log.i(getClass().getName(), "REGISTER SUCCEEDED: " + requestCode + " " + resultCode + " " + data);
                // set username and password to result from Register:
                String newUser = data.getStringExtra(LoginActivity.LOGGED_IN_USER);
                String newPass = data.getStringExtra(LoginActivity.LOGGED_IN_PASSWORD);
                if(newUser != null && !newUser.isEmpty()) {
                    ((EditText)findViewById(R.id.user_name)).setText(newUser);
                }
                if(newPass != null && !newPass.isEmpty()) {
                    ((EditText)findViewById(R.id.password)).setText(newPass);
                }
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

    public void onShowPasswordHint() {
        String username = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String passwordHint = datasource.getPasswordHint(username);
        if(passwordHint.length() > 0) {
            Toast toast = Toast.makeText(getApplicationContext(), String.format(getString(R.string.login_display_password_hint), passwordHint), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.login_password_hint_enter_valid_username), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    public void addTextChangedListener() {
        EditText passwordEditText = (EditText)findViewById(R.id.password);

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText usernameEditText = (EditText)findViewById(R.id.user_name);
                EditText passwordEditText = (EditText)findViewById(R.id.password);
                String username = usernameEditText.getText().toString();

                if(checkLogin(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()))
                {
                    passwordEditText.setEnabled(false);
                    usernameEditText.setEnabled(false);
                    usernameEditText.setBackgroundColor(Color.GREEN);
                    passwordEditText.setBackgroundColor(Color.GREEN);
                    boolean isRememberMeChecked = ((CheckBox) findViewById(R.id.rememberMeCheckBox)).isChecked();
                    datasource.updateRememberedLastUser(username, isRememberMeChecked);

                    // hide keyboard:
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    // if login is successful, send word to calling Activity that we succeeded:
                    intent.putExtra(LOGGED_IN_USER, username);
                    intent.putExtra(LOGGED_IN_TIME, System.currentTimeMillis());
                    intent.putExtra(USER_EMAIL_ADDRESS, datasource.getEmail(username));
                    startActivity(intent);

                    finish();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onSaveInstanceState: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
        savedInstanceState.putBoolean(LOGGED_IN, logged_in);
        savedInstanceState.putString(LOGGED_IN_USER, logged_in_user);
        savedInstanceState.putLong(LOGGED_IN_TIME, logged_in_time);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Try to restore the state.  Unfortunately, this seems to be called after onStart(), which
     * is called AFTER onCreate().
     * @param savedInstanceState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logged_in = savedInstanceState.getBoolean(LOGGED_IN);
        logged_in_user = savedInstanceState.getString(LOGGED_IN_USER);
        logged_in_time = savedInstanceState.getLong(LOGGED_IN_TIME);
        Log.i(getClass().getName(), "onRestoreInstanceState: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
    }


    public void saveLoginStateToPreferences() {
        prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(LOGGED_IN, logged_in);
        editor.putString(LOGGED_IN_USER, logged_in_user);
        editor.putLong(LOGGED_IN_TIME, logged_in_time);
        editor.commit();
    }

    public void loadLoginStateFromPreferences() {
        prefs = getPreferences(MODE_PRIVATE);
        logged_in = prefs.getBoolean(LOGGED_IN, false);
        logged_in_user = prefs.getString(LOGGED_IN_USER, "");
        logged_in_time = prefs.getLong(LOGGED_IN_TIME, 0);
    }

}
