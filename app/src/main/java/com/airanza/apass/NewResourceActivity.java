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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.sql.SQLException;

public class NewResourceActivity extends ActionBarActivity {
    public final static String EXTRA_RESOURCE = "com.airanza.apass.RESOURCE";

    private ResourceDataSource datasource;

    private boolean isPasswordVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_resource);
        try {
            datasource = new ResourceDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        // ADD ADS:
        AdView mBottomAdView = (AdView) findViewById(R.id.new_bottom_adview);
        AdRequest bottomAdRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("03E2E4F5EE38F1A8EF3355F642CCBA94")
                .build();
        mBottomAdView.loadAd(bottomAdRequest);
    }

    public void onClickOnPassword(View view) {
        final EditText editTextPassword = (EditText)findViewById(R.id.password);

        if(!isPasswordVisible) {  // show password
            editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {                  // Hide password
            editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        editTextPassword.setSelection(editTextPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // context-specific actionbar items:
        getMenuInflater().inflate(R.menu.menu_new_resource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_save_resource) {
            saveEntry(null);
            return true;
        }

        if(id == R.id.action_cancel_resource) {
            cancelEntry(null);
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveEntry(View view) {
        Resource resource = datasource.createResource(
                ((EditText)findViewById(R.id.resource_name)).getText().toString(),  // resource name
                ((EditText)findViewById(R.id.username)).getText().toString(),       // username
                ((EditText)findViewById(R.id.password)).getText().toString(),       // password
                ((EditText)findViewById(R.id.description)).getText().toString());   // description

        Toast.makeText(getApplicationContext(), "Saved [" + resource + "]", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onResume() {
        try {
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    public void cancelEntry(View view) {
        finish();
    }
}
