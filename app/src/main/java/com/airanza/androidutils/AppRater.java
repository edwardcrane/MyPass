package com.airanza.androidutils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airanza.apass.R;

public class AppRater {

    private static final String PREF_LAUNCH_COUNT = "launch_count";
    private static final String PREF_EVENT_COUNT = "event_count";
    private static final String PREF_RATE_CLICKED = "rateclicked";
    private static final String PREF_DONT_SHOW = "dontshow";
    private static final String PREF_DATE_REMINDER_PRESSED = "date_reminder_pressed";
    private static final String PREF_DATE_FIRST_LAUNCHED = "date_firstlaunch";
    private static final String PREF_APP_VERSION_CODE = "versioncode";

    private static boolean mIsTestMode = false;

    private static int apprater_days_until_prompt = 30;
    private static int apprater_launches_until_prompt = 15;
    private static int apprater_events_until_prompt = 15;
    private static int apprater_days_before_reminding = 3;

    public static void appLaunched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName()+".apprater", 0);
        if(!mIsTestMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {return;}

        SharedPreferences.Editor editor = prefs.edit();

        if(mIsTestMode){
            showRateDialog(mContext, editor);
            return;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0);

        // Get events counter
        long event_count = prefs.getLong(PREF_EVENT_COUNT, 0);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCHED, 0);

        // Get reminder date pressed
        long date_reminder_pressed = prefs.getLong(PREF_DATE_REMINDER_PRESSED, 0);

        try{
            int appVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            if(prefs.getInt(PREF_APP_VERSION_CODE, 0)  != appVersionCode){
                //Reset the launch and event counters to help assure users are rating based on the latest version.
                launch_count = 0;
                event_count = 0;
                editor.putLong(PREF_EVENT_COUNT, event_count);
            }
            editor.putInt(PREF_APP_VERSION_CODE, appVersionCode);
        }catch(Exception e){
            //do nothing
        }

        launch_count++;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCHED, date_firstLaunch);
        }

        // Wait at least n days or m events before opening
        if (launch_count >= apprater_launches_until_prompt) {
            long millisecondsToWait = apprater_days_until_prompt * 24 * 60 * 60 * 1000L;
            if(System.currentTimeMillis() >= (date_firstLaunch + millisecondsToWait) || event_count >= apprater_events_until_prompt) {
                if(date_reminder_pressed == 0){
                    showRateDialog(mContext, editor);
                } else {
                    long remindMillisecondsToWait = apprater_days_before_reminding * 24 * 60 * 60 * 1000L;
                    if(System.currentTimeMillis() >= (remindMillisecondsToWait + date_reminder_pressed)){
                        showRateDialog(mContext, editor);
                    }
                }
            }
        }

        editor.commit();
    }

    public static void rateApp(Context mContext)
    {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName()+".appirater", 0);
        SharedPreferences.Editor editor = prefs.edit();
        rateApp(mContext, editor);
    }

    public static void significantEvent(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName()+".apprater", 0);
        if(!mIsTestMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {return;}

        long event_count = prefs.getLong(PREF_EVENT_COUNT, 0);
        event_count++;
        prefs.edit().putLong(PREF_EVENT_COUNT, event_count).apply();
    }

    private static void rateApp(Context mContext, final SharedPreferences.Editor editor) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(mContext.getString(R.string.apprater_market_url), mContext.getPackageName()))));
        if (editor != null) {
            editor.putBoolean(PREF_RATE_CLICKED, true);
            editor.commit();
        }
    }

    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);

        if (android.os.Build.VERSION.RELEASE.startsWith("1.") || android.os.Build.VERSION.RELEASE.startsWith("2.0") || android.os.Build.VERSION.RELEASE.startsWith("2.1")){
            //No dialog title on pre-froyo devices
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }else if(mContext.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_LOW || mContext.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_MEDIUM){
            Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            if(rotation == 90 || rotation == 270){
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            }else{
                dialog.setTitle(String.format(mContext.getString(R.string.apprater_rate_title), mContext.getString(R.string.app_name)));
            }
        }else{
            dialog.setTitle(String.format(mContext.getString(R.string.apprater_rate_title), mContext.getString(R.string.app_name)));
        }

        LinearLayout layout = new LinearLayout(mContext);

        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = 300;
        params.height = ViewGroup.LayoutParams.FILL_PARENT;

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setDividerPadding(10);

        TextView tv = new TextView(mContext);
        tv.setText(mContext.getString(R.string.apprater_rate_message));

        Button rateButton = new Button(mContext);
        rateButton.setText(mContext.getString(R.string.apprater_rate_now));
        rateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rateApp(mContext, editor);
                dialog.dismiss();
            }
        });

        Button rateLaterButton = new Button(mContext);
        rateLaterButton.setText(mContext.getString(R.string.apprater_rate_later));
        rateLaterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putLong(PREF_DATE_REMINDER_PRESSED,System.currentTimeMillis());
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        Button cancelButton = new Button(mContext);

        cancelButton.setText(mContext.getString(R.string.apprater_rate_cancel));
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean(PREF_DONT_SHOW, true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });

        layout.addView(tv);
        layout.addView(rateButton);
        layout.addView(rateLaterButton);
        layout.addView(cancelButton);

        dialog.setContentView(layout, params);
        dialog.show();
    }
}
