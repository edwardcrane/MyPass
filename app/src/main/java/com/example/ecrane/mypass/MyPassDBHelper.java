package com.example.ecrane.mypass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ecrane on 1/8/2015.
 */
public class MyPassDBHelper extends SQLiteOpenHelper {
    // if you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MyPass.db";
    public static final String TABLE_NAME = "mypassentry";
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME_ENTRY_ID = "mypassid";
    public static final String COLUMN_NAME_RESOURCENAME = "resourcename";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_PASSWORD = "password";

    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE mypassentry (" +
//                    MyPassDBContract.MyPassEntry._ID + " INTEGER PRIMARY KEY," +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_RESOURCENAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_PASSWORD + TEXT_TYPE +
                    " )";

    public MyPassDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyPassDBHelper.class.getName(), " Upgrading database from version " + oldVersion + " to version " + newVersion + ", which will destroy all old data.");
        // this database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
