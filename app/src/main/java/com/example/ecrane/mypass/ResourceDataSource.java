package com.example.ecrane.mypass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecrane on 1/26/2015.
 */
public class ResourceDataSource {
    private SQLiteDatabase database;
    private MyPassDBHelper dbHelper;

    private String[] allColumns = {
        MyPassDBHelper.COLUMN_ID,
        MyPassDBHelper.COLUMN_NAME_ENTRY_ID,
        MyPassDBHelper.COLUMN_NAME_RESOURCENAME,
        MyPassDBHelper.COLUMN_NAME_DESCRIPTION,
        MyPassDBHelper.COLUMN_NAME_USERNAME,
        MyPassDBHelper.COLUMN_NAME_PASSWORD
    };

    public ResourceDataSource(Context context) {
        dbHelper = new MyPassDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Resource createResource(String entryID, String resourceName, String userName, String password, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // CREATE A NEW MAP OF VALUES, WHERE COLUMN NAMES ARE THE KEYS
        ContentValues values = new ContentValues();
        values.put(MyPassDBHelper.COLUMN_NAME_ENTRY_ID, entryID);
        values.put(MyPassDBHelper.COLUMN_NAME_RESOURCENAME, resourceName);
        values.put(MyPassDBHelper.COLUMN_NAME_USERNAME, userName);
        values.put(MyPassDBHelper.COLUMN_NAME_PASSWORD, password);
        values.put(MyPassDBHelper.COLUMN_NAME_DESCRIPTION, description);

        long newRowId;
        newRowId = db.insert(
                MyPassDBHelper.TABLE_NAME,
                null,
                values);

        Cursor cursor = database.query(MyPassDBHelper.TABLE_NAME,
                allColumns, MyPassDBHelper.COLUMN_ID + " = " + newRowId, null, null, null, null);
        cursor.moveToFirst();
        Resource res = cursorToResource(cursor);
        cursor.close();

        return(res);
    }

    public void update(Resource resource) {
        long id = resource.getID();

        String filterString = "_id=" + id;
        ContentValues args = new ContentValues();
        args.put(MyPassDBHelper.COLUMN_NAME_ENTRY_ID, resource.getEntryID());
        args.put(MyPassDBHelper.COLUMN_NAME_RESOURCENAME, resource.getResourceName());
        args.put(MyPassDBHelper.COLUMN_NAME_USERNAME, resource.getUsername());
        args.put(MyPassDBHelper.COLUMN_NAME_PASSWORD, resource.getPassword());
        args.put(MyPassDBHelper.COLUMN_NAME_DESCRIPTION, resource.getDescription());

        database.update(MyPassDBHelper.TABLE_NAME, args, filterString, null);

        Log.w(this.getClass().getName(), "Updated: " + resource.getResourceName());
    }

    public void deleteResource(Resource resource) {
        long id = resource.getID();
        int i = database.delete(MyPassDBHelper.TABLE_NAME, MyPassDBHelper.COLUMN_ID + " = " + id, null);
        if(i != 1) {
            Log.w(this.getClass().getName(), "Deleting [" + resource + "] failed.  Delete returned [" + i + "] rows.");
        }
    }

    public List<Resource> findResources(String findString) {
        List<Resource> resources = new ArrayList<Resource>();

        String[] projection = {
                MyPassDBHelper.COLUMN_ID,
                MyPassDBHelper.COLUMN_NAME_ENTRY_ID,
                MyPassDBHelper.COLUMN_NAME_RESOURCENAME,
                MyPassDBHelper.COLUMN_NAME_USERNAME,
                MyPassDBHelper.COLUMN_NAME_PASSWORD,
                MyPassDBHelper.COLUMN_NAME_DESCRIPTION
        };
        String whereClause = MyPassDBHelper.COLUMN_NAME_RESOURCENAME + " LIKE \'%"+findString+"%\' OR " + MyPassDBHelper.COLUMN_NAME_DESCRIPTION + " LIKE \'%" + findString + "%\'";

        String sortOrder = MyPassDBHelper.COLUMN_ID + " ASC";

        Cursor cursor = database.query(
                MyPassDBHelper.TABLE_NAME,    // the table to query
                projection,                                 // the columns to return
                whereClause,            //selection,        // the values for the WHERE clause
                null,              //selectionArgs,    // the values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                sortOrder                                   // The sort order
        );

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Resource resource = this.cursorToResource(cursor);
            resources.add(resource);
            cursor.moveToNext();
        }
        return(resources);
    }


    public List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<Resource>();

        Cursor cursor = database.query(MyPassDBHelper.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Resource resource = cursorToResource(cursor);
            resources.add(resource);
            cursor.moveToNext();
        }
        cursor.close();
        return resources;
    }

    public Resource cursorToResource(Cursor cursor) {
        Resource res = new Resource();
        res.setID(cursor.getLong(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_ID)));
        res.setEntryID(cursor.getString(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_NAME_ENTRY_ID)));
        res.setResourceName(cursor.getString(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_NAME_RESOURCENAME)));
        res.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_NAME_USERNAME)));
        res.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_NAME_PASSWORD)));
        res.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyPassDBHelper.COLUMN_NAME_DESCRIPTION)));
        return res;
    }
}
