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

package com.airanza.mypass;

/**
 * Created by ecrane on 3/9/2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class LoginActivity extends ActionBarActivity {
    static final int REGISTER_REQUEST = 2;

    private LoginDataSource datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.activity_login);

        TextView linkToRegisterTextView = (TextView) findViewById(R.id.link_to_register);

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        // if a user is already registered, then don't allow new user.
        if(datasource.logins() > 0) {
            linkToRegisterTextView.setVisibility(View.INVISIBLE);
        } else {
            // Listening to register new account link
            linkToRegisterTextView.setOnClickListener(new View.OnClickListener() {

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

    public void onShowPasswordHintButtonClick(View view) {
        String username = ((EditText) findViewById(R.id.user_name)).getText().toString();
        String passwordHint = datasource.getPasswordHint(username);
        if(passwordHint.length() > 0) {
            Toast.makeText(getApplicationContext(), "Password Hint: [" + passwordHint + "]", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please specify valid User Name.", Toast.LENGTH_LONG).show();
        }
    }

    public void addTextChangedListener() {
        EditText passwordEditText = (EditText)findViewById(R.id.password);

        Log.w(this.getClass().getName(), "addTextChangedListener to passwordEditText");

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText usernameEditText = (EditText)findViewById(R.id.user_name);
                EditText passwordEditText = (EditText)findViewById(R.id.password);
                String username = usernameEditText.getText().toString();

                if(checkLogin(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()))
                {
                    boolean isRememberMeChecked = ((CheckBox) findViewById(R.id.rememberMeCheckBox)).isChecked();
                    datasource.updateRememberedLastUser(username, isRememberMeChecked);

                    // hide keyboard:
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

                    Intent result = new Intent("com.airanza.mypass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

                    // if login is successful, send word to calling Activity that we succeeded:
                    result.putExtra("logged_in_username", username);
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, result);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, result);
                    }

                    finish();
                }
            }
        });
    }
}
