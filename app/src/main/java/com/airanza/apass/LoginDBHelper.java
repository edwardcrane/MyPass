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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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

    /**
     * getPath
     * @return the fully qualified file name of the application database.
     */
    public static String getPath() {
        File data = Environment.getDataDirectory();
        String path = data.getPath() + "/data/" + "com.airanza.apass" + "/databases/" + DATABASE_NAME;
        return(path);
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
