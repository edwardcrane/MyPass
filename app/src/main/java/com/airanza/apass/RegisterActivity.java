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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

        EditText usernameEditText = (EditText) findViewById(R.id.reg_username);
        EditText passwordEditText = (EditText) findViewById(R.id.reg_password);
        EditText confirmPasswordEditText = (EditText) findViewById(R.id.reg_confirm_password);
        EditText passwordHintEditText = (EditText) findViewById(R.id.reg_password_hint);
        EditText emailEditText = (EditText) findViewById(R.id.reg_email);

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        String newPassword = "";
        String passwordHint = passwordHintEditText.getText().toString();
        String email = emailEditText.getText().toString();

        Intent intent = getIntent();
        int actionCode = intent.getIntExtra(LoginActivity.REGISTER_ACTION, LoginActivity.LOGIN_REQUEST);
        String oldUsername = intent.getStringExtra(LoginActivity.LOGGED_IN_USER);

        Intent result = new Intent("com.airanza.apass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        if((actionCode != LoginActivity.CHANGE_LOGIN && !password.equals(confirmPassword)) ||
                (actionCode == LoginActivity.CHANGE_LOGIN && !newPassword.equals(confirmPassword))) {
            Toast t = Toast.makeText(getApplicationContext(), getText(R.string.register_password_and_confirm_do_not_match), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            passwordEditText.setText("");
            confirmPasswordEditText.setText("");
            passwordEditText.requestFocus();
            return;
        }

        // ensure that user enters all three field values:
        if(username.length() <= 0 ||
                password.length() <= 0 ||
                ((actionCode != LoginActivity.CHANGE_LOGIN) && confirmPassword.length() <=0) ||
                ((actionCode == LoginActivity.CHANGE_LOGIN) && (newPassword.length() <= 0)
                    && (confirmPassword.length() <=0)) ||
                passwordHint.length() <= 0 ||
                email.length() <= 0) {
            // display an error message and allow the user to try again:
            Toast t = Toast.makeText(getApplicationContext(), getText(R.string.register_fields_must_be_completed), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }

        // we cannot have duplicate login:
        if(datasource.isExistingUsername(username)  && actionCode == LoginActivity.LOGIN_REQUEST) {
            // display an error message and allow the user to try again:
            Toast t = Toast.makeText(getApplicationContext(), String.format(getString(R.string.register_user_already_exists), username), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
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
        if(datasource.isValidLogin(username, password)) {
            // if good login created, send username & password back to caller LoginActivity:
            result.putExtra(LoginActivity.LOGGED_IN_USER, username);
            result.putExtra(LoginActivity.LOGGED_IN_PASSWORD, password);
        } else {
            // display an error message and allow the user to try again or cancel:
            Toast t = Toast.makeText(getApplicationContext(), String.format(getString(R.string.register_user_was_not_created), username), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            Log.w(this.getClass().getName(), String.format(getString(R.string.register_user_was_not_created), username));
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
