package com.airanza.mypass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ecrane on 1/8/2015.
 */
public class LoginDBHelper extends SQLiteOpenHelper {
    // if you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Login.db";
    public static final String TABLE_NAME = "login";
    public static final String SQL_DELETE_LOGIN = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_PASSWORD = "password";
    public static final String COLUMN_NAME_PASSWORD_HINT = "password_hint";
    public static final String COLUMN_NAME_EMAIL = "email";
    public static final String COLUMN_NAME_REMEMBERED_LAST_USER = "remembered_last_user";

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_LOGIN =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_PASSWORD + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_PASSWORD_HINT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_REMEMBERED_LAST_USER + INTEGER_TYPE +
                    " )";

    public LoginDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOGIN);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LoginDBHelper.class.getName(), " Upgrading database from version " + oldVersion + " to version " + newVersion + ", which will destroy all old data.");
        // this database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over
        db.execSQL(SQL_DELETE_LOGIN);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
