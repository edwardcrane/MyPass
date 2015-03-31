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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

public class RegisterActivity extends Activity {

    private LoginDataSource datasource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        TextView newPasswordTextView = (TextView) findViewById(R.id.reg_new_password_label);
        EditText newPasswordEditText = (EditText) findViewById(R.id.reg_new_password);
        Button registerButton = (Button) findViewById(R.id.btnRegister);
        if(intent.getIntExtra(MainActivity.REGISTER_ACTION, MainActivity.LOGIN_REQUEST) == MainActivity.CHANGE_LOGIN) {
            newPasswordTextView.setVisibility(View.VISIBLE);
            newPasswordTextView.setEnabled(true);
            newPasswordEditText.setVisibility(View.VISIBLE);
            newPasswordEditText.setEnabled(true);
            registerButton.setText("Change Login Information");
        } else {
            // remove new_password and label:
            ViewGroup vg = (ViewGroup)newPasswordTextView.getParent();
            vg.removeView(newPasswordTextView);
            vg.removeView(newPasswordEditText);
            registerButton.setText("Create New Login");
        }

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
        String confirmPassword = ((EditText) findViewById(R.id.reg_confirm_password)).getText().toString();
        String newPassword = ((EditText) findViewById(R.id.reg_new_password)).getText().toString();
        String passwordhint = ((EditText) findViewById(R.id.reg_password_hint)).getText().toString();
        String email = ((EditText) findViewById(R.id.reg_email)).getText().toString();

        Intent intent = getIntent();
        int actionCode = intent.getIntExtra(MainActivity.REGISTER_ACTION, MainActivity.LOGIN_REQUEST);
        String oldUsername = intent.getStringExtra(MainActivity.LOGGED_IN_USER);

        Intent result = new Intent("com.airanza.mypass.MainActivity.LOGIN_REQUEST", Uri.parse("content://result_uri"));

        if(!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Password and Confirmation do not match!", Toast.LENGTH_LONG).show();
            return;
        }

        // ensure that user enters all three field values:
        if(username.length() <= 0 ||
                password.length() <= 0 ||
                confirmPassword.length() <= 0 ||
                ((actionCode == MainActivity.CHANGE_LOGIN) && (newPassword.length() <= 0)) ||
                passwordhint.length() <= 0 ||
                email.length() <= 0) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "All fields must be completed.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // we cannot have duplicate login:
        if(datasource.isExistingUsername(username)  && actionCode == MainActivity.LOGIN_REQUEST) {
            // display an error message and allow the user to try again:
            Toast.makeText(getApplicationContext(), "User Name [" + username + "] already exists.  Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (actionCode == MainActivity.LOGIN_REQUEST) {
            datasource.createLogin(username, password, passwordhint, email);
        } else if(actionCode == MainActivity.CHANGE_LOGIN) {
            datasource.update(oldUsername, username, password, newPassword, "old password hint", passwordhint, "old email", email, 0, 1);
            result.putExtra(MainActivity.LOGGED_IN_USER, username);
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
}
