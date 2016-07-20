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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ecrane on 3/31/2015.
 */

public class SplashActivity extends Activity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    public final static String SPLASH_ACTION = "splash_action";
    public final static int SPLASH_ABOUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FacebookSdk.sdkInitialize(getApplicationContext());

        AppEventsLogger.activateApp(this);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView)findViewById(R.id.ver_name)).setText(getString(R.string.app_name) + " " + getString(R.string.splash_version) + " " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }

        ((TextView)findViewById(R.id.terms_of_use_link)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.terms_of_use_link)).setText(Html.fromHtml(getResources().getString(R.string.terms_of_use_url)));

        ((TextView)findViewById(R.id.airanza_link)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.airanza_link)).setText(Html.fromHtml(getResources().getString(R.string.airanza_url)));

        ((TextView) findViewById(R.id.privacy_policy_link)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.privacy_policy_link)).setText(Html.fromHtml(getResources().getString(R.string.privacy_policy_url)));


        int actionCode = getIntent().getIntExtra(SPLASH_ACTION, 0);
        if(actionCode != SPLASH_ABOUT) {
            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);

                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }
}
