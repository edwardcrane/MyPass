package com.airanza.mypass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by ecrane on 1/26/2015.
 */
public class LoginDataSource {
    private SQLiteDatabase database;
    private LoginDBHelper dbHelper;

    private String[] allColumns = {
        LoginDBHelper.COLUMN_ID,
        LoginDBHelper.COLUMN_NAME_USERNAME,
        LoginDBHelper.COLUMN_NAME_PASSWORD,
        LoginDBHelper.COLUMN_NAME_EMAIL
    };

    public LoginDataSource(Context context) {
        dbHelper = new LoginDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createLogin(String userName, String password, String password_hint, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // CREATE A NEW MAP OF VALUES, WHERE COLUMN NAMES ARE THE KEYS
        ContentValues values = new ContentValues();
        values.put(LoginDBHelper.COLUMN_NAME_USERNAME, userName);
        values.put(LoginDBHelper.COLUMN_NAME_PASSWORD, password);
        values.put(LoginDBHelper.COLUMN_NAME_PASSWORD_HINT, password_hint);
        values.put(LoginDBHelper.COLUMN_NAME_EMAIL, email);
        values.put(LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER, 0);

        long newRowId;
        newRowId = db.insert(
                LoginDBHelper.TABLE_NAME,
                null,
                values);
    }

    public void update(String username, String newUsername, String password, String newPassword, String passwordHint, String newPasswordHint, String email, String newEmail, int rememberMe, int newRememberMe) {
        String filterString = LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\'";
        ContentValues args = new ContentValues();
        args.put(LoginDBHelper.COLUMN_NAME_USERNAME, newUsername);
        args.put(LoginDBHelper.COLUMN_NAME_PASSWORD, newPassword);
        args.put(LoginDBHelper.COLUMN_NAME_PASSWORD_HINT, newPasswordHint);
        args.put(LoginDBHelper.COLUMN_NAME_EMAIL, newEmail);
        args.put(LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER, newRememberMe);

        database.update(LoginDBHelper.TABLE_NAME, args, filterString, null);

        Log.w(this.getClass().getName(), "Updated: " + newUsername);
    }

    public void updateRememberedLastUser(String username, boolean rememberMe) {
        String filterString = LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\'";
        ContentValues args = new ContentValues();
        if(rememberMe) {
            args.put(LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER, 1);
        } else {
            args.put(LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER, 0);
        }
        database.update(LoginDBHelper.TABLE_NAME, args, filterString, null);
        Log.v(this.getClass().getName(), "Updated: [" + username + "] rememberMe[" + rememberMe + "]");
    }

    public String getRememberedLastUser() {
        String lastUser = "";

        if(logins() <= 0) {
            return "";
        }

        String[] projection = {
                LoginDBHelper.COLUMN_NAME_USERNAME,
                LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER
        };
        String whereClause = LoginDBHelper.COLUMN_NAME_REMEMBERED_LAST_USER + " = " + 1;

        Cursor cursor = database.query(
                LoginDBHelper.TABLE_NAME,
                projection,
                whereClause,
                null,
                null,
                null,
                null
        );
        if(cursor.getCount() == 0) {
            // no "remembered" username
            cursor.close();
            return "";
        }
        cursor.moveToFirst();
        lastUser = cursor.getString(cursor.getColumnIndexOrThrow(LoginDBHelper.COLUMN_NAME_USERNAME));
        cursor.close();
        return lastUser;
    }

    public void deleteLogin(String username) {
        int i = database.delete(LoginDBHelper.TABLE_NAME, LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\'", null);
        if(i != 1) {
            Log.w(this.getClass().getName(), "Deleting [" + username + "] failed.  Delete returned [" + i + "] rows.");
        }
    }

    public boolean isExistingUsername(String username) {
        boolean usernameMatched = false;
        String[] projection = {
                LoginDBHelper.COLUMN_NAME_USERNAME
        };
        String whereClause = LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\'";

        Cursor cursor = database.query(
                LoginDBHelper.TABLE_NAME,
                projection,
                whereClause,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            usernameMatched = true;
            cursor.moveToNext();
        }
        return usernameMatched;
    }

    public boolean isValidLogin(String username, String password) {
        boolean loginMatched = false;
        String[] projection = {
                LoginDBHelper.COLUMN_NAME_USERNAME,
                LoginDBHelper.COLUMN_NAME_PASSWORD
        };
        String whereClause = LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\' AND " +
                LoginDBHelper.COLUMN_NAME_PASSWORD + " = \'" + password + "\'";

        Cursor cursor = database.query(
                LoginDBHelper.TABLE_NAME,
                projection,
                whereClause,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            loginMatched = true;
            cursor.moveToNext();
        }
        return loginMatched;
    }

    public int logins() {
        int num_logins = 0;
        String[] projection = {
                LoginDBHelper.COLUMN_NAME_USERNAME
        };

        Cursor cursor = database.query(
                LoginDBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            num_logins++;
            cursor.moveToNext();
        }
        return num_logins;
    }

    public String getEmail(String username) {
        String email = null;
        if(!isExistingUsername(username)) {
            return("");
        }

        String[] projection = {
                LoginDBHelper.COLUMN_NAME_USERNAME,
                LoginDBHelper.COLUMN_NAME_EMAIL
        };
        String whereClause = LoginDBHelper.COLUMN_NAME_USERNAME + " = \'" + username + "\'";

        Cursor cursor = database.query(
                LoginDBHelper.TABLE_NAME,
                projection,
                whereClause,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        email = cursor.getString(cursor.getColumnIndexOrThrow(LoginDBHelper.COLUMN_NAME_EMAIL));
        cursor.close();
        return email;
    }
}