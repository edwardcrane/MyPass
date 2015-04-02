package com.airanza.mypass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Created by ecrane on 3/31/2015.
 */

public class SplashActivity extends Activity {
    private static boolean splashLoaded = false;

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(splashLoaded) {
            // only show the splash screen once, at startup.
            // todo:  this doesn't quite work, as it seems to save splashLoaded == true across app runs.
            // figure this out.  Setting it to false in onDestroy() also doesn't work, as finish() causes
            // onDestroy to be called.
            finish();
        }

        setContentView(R.layout.activity_splash);
        splashLoaded = true;

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
//                Intent i = new Intent(SplashActivity.this, MainActivity.class);
//                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
