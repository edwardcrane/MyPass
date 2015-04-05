/*
 * Copyright (c) 2015.
 *
 * AIRANZA, INC.
 * -------------
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

package com.airanza.mypass;

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
    private ResourceDBHelper dbHelper;

    private String[] allColumns = {
        ResourceDBHelper.COLUMN_ID,
        ResourceDBHelper.COLUMN_NAME_RESOURCENAME,
        ResourceDBHelper.COLUMN_NAME_DESCRIPTION,
        ResourceDBHelper.COLUMN_NAME_USERNAME,
        ResourceDBHelper.COLUMN_NAME_PASSWORD
    };

    public ResourceDataSource(Context context) {
        dbHelper = new ResourceDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Resource createResource(String resourceName, String userName, String password, String description) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // CREATE A NEW MAP OF VALUES, WHERE COLUMN NAMES ARE THE KEYS
        ContentValues values = new ContentValues();
        values.put(ResourceDBHelper.COLUMN_NAME_RESOURCENAME, resourceName);
        values.put(ResourceDBHelper.COLUMN_NAME_USERNAME, userName);
        values.put(ResourceDBHelper.COLUMN_NAME_PASSWORD, password);
        values.put(ResourceDBHelper.COLUMN_NAME_DESCRIPTION, description);

        long newRowId;
        newRowId = db.insert(
                ResourceDBHelper.TABLE_NAME,
                null,
                values);

        Cursor cursor = database.query(ResourceDBHelper.TABLE_NAME,
                allColumns, ResourceDBHelper.COLUMN_ID + " = " + newRowId, null, null, null, null);
        cursor.moveToFirst();
        Resource res = cursorToResource(cursor);
        cursor.close();

        return(res);
    }

    public void update(Resource resource) {
        long id = resource.getID();

        String filterString = "_id=" + id;
        ContentValues args = new ContentValues();
        args.put(ResourceDBHelper.COLUMN_NAME_RESOURCENAME, resource.getResourceName());
        args.put(ResourceDBHelper.COLUMN_NAME_USERNAME, resource.getUsername());
        args.put(ResourceDBHelper.COLUMN_NAME_PASSWORD, resource.getPassword());
        args.put(ResourceDBHelper.COLUMN_NAME_DESCRIPTION, resource.getDescription());

        database.update(ResourceDBHelper.TABLE_NAME, args, filterString, null);

        Log.w(this.getClass().getName(), "Updated: " + resource.getResourceName());
    }

    public void deleteResource(Resource resource) {
        long id = resource.getID();
        int i = database.delete(ResourceDBHelper.TABLE_NAME, ResourceDBHelper.COLUMN_ID + " = " + id, null);
        if(i != 1) {
            Log.w(this.getClass().getName(), "Deleting [" + resource + "] failed.  Delete returned [" + i + "] rows.");
        }
    }

    public List<Resource> findResources(String findString) {
        List<Resource> resources = new ArrayList<Resource>();

        String[] projection = {
                ResourceDBHelper.COLUMN_ID,
                ResourceDBHelper.COLUMN_NAME_RESOURCENAME,
                ResourceDBHelper.COLUMN_NAME_USERNAME,
                ResourceDBHelper.COLUMN_NAME_PASSWORD,
                ResourceDBHelper.COLUMN_NAME_DESCRIPTION
        };
        String whereClause = ResourceDBHelper.COLUMN_NAME_RESOURCENAME + " LIKE \'%"+findString+"%\' OR " + ResourceDBHelper.COLUMN_NAME_DESCRIPTION + " LIKE \'%" + findString + "%\'";

        String sortOrder = ResourceDBHelper.COLUMN_NAME_RESOURCENAME + " COLLATE NOCASE ASC";

        Cursor cursor = database.query(
                ResourceDBHelper.TABLE_NAME,    // the table to query
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

        Cursor cursor = database.query(ResourceDBHelper.TABLE_NAME, allColumns, null, null, null, null, null);
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
        res.setID(cursor.getLong(cursor.getColumnIndexOrThrow(ResourceDBHelper.COLUMN_ID)));
        res.setResourceName(cursor.getString(cursor.getColumnIndexOrThrow(ResourceDBHelper.COLUMN_NAME_RESOURCENAME)));
        res.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(ResourceDBHelper.COLUMN_NAME_USERNAME)));
        res.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(ResourceDBHelper.COLUMN_NAME_PASSWORD)));
        res.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(ResourceDBHelper.COLUMN_NAME_DESCRIPTION)));
        return res;
    }
}
