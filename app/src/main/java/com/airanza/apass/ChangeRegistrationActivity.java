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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.sql.SQLException;

public class ChangeRegistrationActivity extends ActionBarActivity {

    private LoginDataSource datasource;

    private String newUsername = "";
    private String newPassword = "";
    private String newPasswordHint = "";
    private String newEmail = "";

    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_registration);

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
            return;
        }

        TextView usernameTextView = (TextView) findViewById(R.id.change_reg_username);
        TextView passwordTextView = (TextView) findViewById(R.id.change_reg_password);
        TextView passwordHintTextView = (TextView) findViewById(R.id.change_reg_password_hint);
        TextView emailTextView = (TextView) findViewById(R.id.change_reg_email);

        Intent intent = getIntent();

        // setup username with current username:
        usernameTextView.setText(intent.getStringExtra(LoginActivity.LOGGED_IN_USER));

        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeUsernameRequested(v);
            }
        });

        passwordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangePasswordRequested(v);
            }
        });

        passwordHintTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangePasswordRequested(v);
            }
        });

        emailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeEmailRequested(v);
            }
        });


        passwordTextView.setText(datasource.getPassword(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));
        passwordHintTextView.setText(datasource.getPasswordHint(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));
        emailTextView.setText(datasource.getEmail(intent.getStringExtra(LoginActivity.LOGGED_IN_USER)));
    }

    public void onChangeEmailRequested(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_registration_change_email_text);
        final String originalUsername = ((TextView)findViewById(R.id.change_reg_username)).getText().toString();

        final EditText emailEditText = new EditText(this);
        emailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEditText.setText(((TextView)findViewById(R.id.change_reg_email)).getText().toString());
        emailEditText.setHint(R.string.change_registration_email_hint);

        final EditText confirmEmailEditText = new EditText(this);
        confirmEmailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        confirmEmailEditText.setHint(R.string.change_registration_confirm_email_hint);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(emailEditText);
        layout.addView(confirmEmailEditText);
        builder.setView(layout);

        builder.setPositiveButton(R.string.change_registration_change_username_dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newEmail = emailEditText.getText().toString();
                if (!newEmail.isEmpty() && newEmail.equals(confirmEmailEditText.getText().toString())) {
                    datasource.changeEmail(originalUsername, newEmail);
                    ((TextView) findViewById(R.id.change_reg_email)).setText(newEmail);
                    Toast.makeText(getApplicationContext(), R.string.change_registration_change_email_successful, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), R.string.change_registration_change_username_successful, Toast.LENGTH_LONG).show();
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

    public void onChangePasswordRequested(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_registration_change_password_text);

        final String originalUsername = ((TextView)findViewById(R.id.change_reg_username)).getText().toString();
        final String originalPasswordHint = ((TextView)findViewById(R.id.change_reg_password_hint)).getText().toString();

        final TextView usernameTextView = new TextView(this);
        usernameTextView.setText(originalUsername);

        final EditText oldPasswordEditText = new EditText(this);
        oldPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPasswordEditText.setHint(R.string.change_registration_old_password_hint);

        final EditText newPasswordEditText = new EditText(this);
        newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordEditText.setHint(R.string.change_registration_new_password_hint);

        final EditText confirmNewPasswordEditText = new EditText(this);
        confirmNewPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmNewPasswordEditText.setHint(R.string.change_registration_confirm_new_password_hint);

        final EditText newPasswordHintEditText = new EditText(this);
        newPasswordHintEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        newPasswordHintEditText.setHint(R.string.change_registration_password_hint_hint);
        newPasswordHintEditText.setText(originalPasswordHint);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(usernameTextView);
        layout.addView(oldPasswordEditText);
        layout.addView(newPasswordEditText);
        layout.addView(confirmNewPasswordEditText);
        layout.addView(newPasswordHintEditText);
        builder.setView(layout);

        builder.setPositiveButton(R.string.change_registration_change_username_dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPassword = newPasswordEditText.getText().toString();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString();
                newPasswordHint = newPasswordHintEditText.getText().toString();
                if(!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(getApplicationContext(), R.string.change_registration_password_confirm_do_not_match, Toast.LENGTH_LONG).show();
                    return;
                }

                if(datasource.isValidLogin(originalUsername, oldPasswordEditText.getText().toString())) {
                    // old login information was valid, so go ahead and change the password...
                    datasource.changePassword(originalUsername, newPassword);
                    datasource.changePasswordHint(originalUsername, newPasswordHint);
                    if(datasource.isValidLogin(originalUsername, newPassword)) {
                        // success
                        Toast.makeText(getApplicationContext(), R.string.change_registration_password_update_successful, Toast.LENGTH_LONG).show();
                        ((TextView)findViewById(R.id.change_reg_password)).setText(newPassword);
                        ((TextView)findViewById(R.id.change_reg_password_hint)).setText(newPasswordHint);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.change_registration_password_update_failed, Toast.LENGTH_LONG).show();
                    }
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

    @Override
    public void onBackPressed() {
        Intent result = new Intent("com.airanza.apass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));
        result.putExtra(LoginActivity.LOGGED_IN_USER, ((TextView)findViewById(R.id.change_reg_username)).getText().toString());
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }
}
