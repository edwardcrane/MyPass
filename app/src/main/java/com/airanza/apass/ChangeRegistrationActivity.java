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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class ChangeRegistrationActivity extends ActionBarActivity {

    private LoginDataSource datasource;

    private String newUsername = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_registration);

        try {
            datasource = new LoginDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
            return;
        }

        TextView usernameTextView = (TextView) findViewById(R.id.change_reg_username);
        EditText passwordHintEditText = (EditText) findViewById(R.id.change_reg_password_hint);
        EditText emailEditText = (EditText) findViewById(R.id.change_reg_email);

        Intent intent = getIntent();

        // setup username with current username:
        usernameTextView.setText(intent.getStringExtra(LoginActivity.LOGGED_IN_USER));

        // setup password hint
        passwordHintEditText.setText(datasource.getPasswordHint(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));

        // setup email address:
        emailEditText.setText(datasource.getEmail(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));
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

    public void onChangeUsernameRequested(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_registration_change_username_button_text);
        final String originalUsername = ((TextView)findViewById(R.id.change_reg_username)).getText().toString();

        final EditText input = new EditText(this);
        input.setText(originalUsername);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton(R.string.change_registration_change_username_dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newUsername = input.getText().toString();
                if (!datasource.isExistingUsername(newUsername)) {
                    datasource.changeUsername(originalUsername, newUsername);
                    if (datasource.getRememberedLastUser().equals(originalUsername)) {
                        datasource.updateRememberedLastUser(newUsername, true);
                    }
                    ((TextView) findViewById(R.id.change_reg_username)).setText(newUsername);
                    Toast.makeText(getApplicationContext(), R.string.change_registration_change_username_succesful, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.change_registration_username_already_exists, Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(R.string.change_registration_change_username_dialog_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void onRegisterButtonClick(View view) {

        Intent intent = getIntent();
        String oldUsername = intent.getStringExtra(LoginActivity.LOGGED_IN_USER);

        // get username and password
        TextView usernameTextView = (TextView) findViewById(R.id.change_reg_username);
        EditText passwordEditText = (EditText) findViewById(R.id.change_reg_password);

        // get new password and confirm:
        EditText newPasswordEditText = (EditText) findViewById(R.id.change_reg_new_password);
        EditText confirmNewPasswordEditText = (EditText) findViewById(R.id.change_reg_confirm_new_password);

        EditText passwordHintEditText = (EditText) findViewById(R.id.change_reg_password_hint);

        EditText emailEditText = (EditText) findViewById(R.id.change_reg_email);
        EditText confirmEmailEditText = (EditText) findViewById(R.id.change_reg_confirm_email);

        String username = usernameTextView.getText().toString();
        String password = passwordEditText.getText().toString();

        String newPassword = newPasswordEditText.getText().toString();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

        String passwordHint = passwordHintEditText.getText().toString();

        String email = emailEditText.getText().toString();
        String confirmEmail = confirmEmailEditText.getText().toString();

        if(!datasource.isValidLogin(username, password)) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.change_registration_username_password_invalid), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            return;
        }

        // if new password is not blank, new password confirmation must match:
        if((!newPassword.equals("")) && !newPassword.equals(confirmNewPassword)) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.change_registration_password_confirm_do_not_match), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            newPasswordEditText.setText("");
            confirmNewPasswordEditText.setText("");
            newPasswordEditText.requestFocus();
            if(passwordHint.equals("")) {
                Toast t1 = Toast.makeText(getApplicationContext(), getString(R.string.change_registration_password_hint_new_password), Toast.LENGTH_LONG);
                t1.setGravity(Gravity.CENTER, 0, 0);
                t1.show();
                passwordHintEditText.requestFocus();
            }
            return;
        }

        // if email is not blank, new email is not same as old email, new email confirmation must match:
        if((!email.equals("")) && !email.equals(datasource.getEmail(oldUsername)) && !email.equals(confirmEmail)) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.change_registration_email_confirm_do_not_match), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            emailEditText.setText("");
            confirmEmailEditText.setText("");
            emailEditText.requestFocus();
            return;
        }

        datasource.update(oldUsername, username, password, newPassword, "old password hint", passwordHint, "old email", email, 0, 1);

        if(!datasource.isValidLogin(username, newPassword)) {
            // display an error message and allow the user to try again or cancel:
            Toast t = Toast.makeText(getApplicationContext(), String.format(getString(R.string.change_registration_username_not_updated), username), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            Log.w(this.getClass().getName(), "LOGIN UPDATE FAILED FOR [" + username + "] [" + password + "] [" + email + "]");
            return;
        } else {
            Toast t = Toast.makeText(getApplicationContext(), String.format(getString(R.string.change_registration_username_updated_successfully), username), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            Log.w(this.getClass().getName(), "USERNAME [" + username + "] [" + password + "] [" + email + "] WAS UPDATED SUCCESSFULLY!");
        }

        Intent result = new Intent("com.airanza.apass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));
        result.putExtra(LoginActivity.LOGGED_IN_USER, username);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, result);
        } else {
            getParent().setResult(Activity.RESULT_OK, result);
        }
        finish();
    }

    public void cancelEntry(View view) {
        Intent result = new Intent("com.airanza.apass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));
        result.putExtra(LoginActivity.LOGGED_IN_USER, ((TextView)findViewById(R.id.change_reg_username)).getText().toString());
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }
}
