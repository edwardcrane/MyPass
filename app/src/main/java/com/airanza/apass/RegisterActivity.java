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
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class RegisterActivity extends ActionBarActivity {

    private LoginDataSource datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
            return;
        }

        EditText usernameEditText = (EditText) findViewById(R.id.reg_fullname);
        EditText passwordEditText = (EditText) findViewById(R.id.reg_password);

        TextView confirmOldPasswordLabelTextView = (TextView)findViewById(R.id.reg_confirm_password_label);
        EditText confirmOldPasswordEditText = (EditText) findViewById(R.id.reg_confirm_password);

        TextView newPasswordTextView = (TextView) findViewById(R.id.reg_new_password_label);
        EditText newPasswordEditText = (EditText) findViewById(R.id.reg_new_password);

        TextView confirmNewPasswordTextView = (TextView) findViewById(R.id.reg_confirm_new_password_label);
        EditText confirmNewPasswordEditText = (EditText) findViewById(R.id.reg_confirm_new_password);

        EditText passwordHintEditText = (EditText) findViewById(R.id.reg_password_hint);
        EditText emailEditText = (EditText) findViewById(R.id.reg_email);

        TextView linkToLogin = (TextView) findViewById(R.id.link_to_login);

        Intent intent = getIntent();
        if(intent.getIntExtra(LoginActivity.REGISTER_ACTION, LoginActivity.LOGIN_REQUEST) == LoginActivity.CHANGE_LOGIN) {

            // hide confirm old password label and field:
            confirmOldPasswordLabelTextView.setVisibility(View.GONE);
            confirmOldPasswordEditText.setVisibility(View.GONE);

            // make new password label and field visible
            newPasswordTextView.setVisibility(View.VISIBLE);
            newPasswordTextView.setEnabled(true);
            newPasswordEditText.setVisibility(View.VISIBLE);
            newPasswordEditText.setEnabled(true);

            // make confirm new password label visible & enabled
            confirmNewPasswordTextView.setVisibility(View.VISIBLE);
            confirmNewPasswordTextView.setEnabled(true);
            confirmNewPasswordTextView.setText("Confirm New Password:");

            // make confirm new password field visible & enabled
            confirmNewPasswordEditText.setVisibility(View.VISIBLE);
            confirmNewPasswordEditText.setEnabled(true);

            // hide link to login:
            linkToLogin.setVisibility(View.INVISIBLE);
            linkToLogin.setEnabled(false);

            // setup username with current username:
            usernameEditText.setText(intent.getStringExtra(LoginActivity.LOGGED_IN_USER));

            // setup password hint
            passwordHintEditText.setText(datasource.getPasswordHint(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));

            // setup email address:
            emailEditText.setText(datasource.getEmail(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));
        } else {
            // remove new_password and label:
            ViewGroup vg = (ViewGroup)newPasswordTextView.getParent();
            vg.removeView(newPasswordTextView);
            vg.removeView(newPasswordEditText);

            // Listening to Login Screen link
            linkToLogin.setOnClickListener(new View.OnClickListener() {

                public void onClick(View arg0) {
                    // grab values and try to create the login:

                    // Switching to Login Screen/closing register screen
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // context-specific actionbar items:
        getMenuInflater().inflate(R.menu.menu_register_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_save_login_info) {
            onRegisterButtonClick(null);
            return true;
        }

        if(id == R.id.action_cancel_login_info) {
            cancelEntry(null);
        }

        return super.onOptionsItemSelected(item);
    }


    public void onRegisterButtonClick(View view) {

        EditText usernameEditText = (EditText) findViewById(R.id.reg_fullname);
        EditText passwordEditText = (EditText) findViewById(R.id.reg_password);
        EditText confirmPasswordEditText = (EditText) findViewById(R.id.reg_confirm_password);
        EditText newPasswordEditText = (EditText) findViewById(R.id.reg_new_password);
        EditText confirmNewPasswordEditText = (EditText) findViewById(R.id.reg_confirm_new_password);
        EditText passwordHintEditText = (EditText) findViewById(R.id.reg_password_hint);
        EditText emailEditText = (EditText) findViewById(R.id.reg_email);

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        String newPassword = "";
        if(getIntent().getIntExtra(LoginActivity.REGISTER_ACTION, LoginActivity.LOGIN_REQUEST) == LoginActivity.CHANGE_LOGIN) {
            newPassword = newPasswordEditText.getText().toString();
        }
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString();
        String passwordHint = passwordHintEditText.getText().toString();
        String email = emailEditText.getText().toString();

        Intent intent = getIntent();
        int actionCode = intent.getIntExtra(LoginActivity.REGISTER_ACTION, LoginActivity.LOGIN_REQUEST);
        String oldUsername = intent.getStringExtra(LoginActivity.LOGGED_IN_USER);

        Intent result = new Intent("com.airanza.apass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        if((actionCode != LoginActivity.CHANGE_LOGIN && !password.equals(confirmPassword)) ||
                (actionCode == LoginActivity.CHANGE_LOGIN && !newPassword.equals(confirmNewPassword))) {
            Toast.makeText(getApplicationContext(), "Password and Confirmation do not match!", Toast.LENGTH_LONG).show();
            return;
        }

        // ensure that user enters all three field values:
        if(username.length() <= 0 ||
                password.length() <= 0 ||
                ((actionCode != LoginActivity.CHANGE_LOGIN) && confirmPassword.length() <=0) ||
                ((actionCode == LoginActivity.CHANGE_LOGIN) && (newPassword.length() <= 0)
                    && (confirmNewPassword.length() <=0)) ||
                passwordHint.length() <= 0 ||
                email.length() <= 0) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "All fields must be completed.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // we cannot have duplicate login:
        if(datasource.isExistingUsername(username)  && actionCode == LoginActivity.LOGIN_REQUEST) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "User Name [" + username + "] already exists.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (actionCode == LoginActivity.LOGIN_REQUEST) {
            datasource.createLogin(username, password, passwordHint, email);
        } else if(actionCode == LoginActivity.CHANGE_LOGIN) {
            datasource.update(oldUsername, username, password, newPassword, "old password hint", passwordHint, "old email", email, 0, 1);
            result.putExtra(LoginActivity.LOGGED_IN_USER, username);
            // now we can throw away the old password.
            password = newPassword;
        }
        if(!datasource.isValidLogin(username, password)) {
            // display an error message and allow the user to try again or cancel:
            Toast.makeText(getApplicationContext(), "USER NAME [" + username + "] WAS NOT CREATED.  Please try again.", Toast.LENGTH_LONG).show();
            Log.w(this.getClass().getName(), "LOGIN CREATION FAILED FOR [" + username + "] [" + password + "] [" + email + "]");
            return;
        }

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, result);
        } else {
            getParent().setResult(Activity.RESULT_OK, result);
        }
        finish();
    }

    public void cancelEntry(View view) {
        finish();
    }
}
