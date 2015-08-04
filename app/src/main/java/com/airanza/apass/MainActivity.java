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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.airanza.androidutils.AndroidEncryptor;
import com.airanza.androidutils.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;

public class MainActivity extends ActionBarActivity {

    private ResourceDataSource resourcedatasource;

    private List<Resource> values = null;
    private ListView listView = null;
    private ResourcesAdapter adapter = null;

    static final int SEND_EMAIL_REQUEST = 2;

    private String logged_in_user = "";

    public final static String EXTRA_RESOURCE = "com.airanza.apass.RESOURCE";

    private final static String EXPORT_FILE_NAME = "exportCSV.csv";

    public final static String APP_EXTENSION = ".apa";  // extension for exported, encrypted files.

    // This is retrieved from logindatasource, but if not found, this is the default:
    private final static String DEFAULT_EXPORT_EMAIL_ADDRESS = "crane.edward@gmail.com";
    private static String user_email_address = DEFAULT_EXPORT_EMAIL_ADDRESS;

    private int nDefaultSearchTextColor = Color.TRANSPARENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nDefaultSearchTextColor = ((EditText)findViewById(R.id.findString)).getTextColors().getDefaultColor();

        // hide keyboard unless explicitly required by user clicking:
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        logged_in_user = getIntent().getStringExtra(LoginActivity.LOGGED_IN_USER);
        user_email_address = getIntent().getStringExtra(LoginActivity.USER_EMAIL_ADDRESS);

        try {
            resourcedatasource = new ResourceDataSource(this);
            resourcedatasource.open();
            values = resourcedatasource.getAllResources();

            adapter = new ResourcesAdapter(getApplicationContext(), values);
            listView = (ListView)findViewById(R.id.listMain);
            listView.setAdapter(adapter);

        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        ListView listView = (ListView)findViewById(R.id.listMain);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), EditResourceActivity.class);
                        intent.putExtra(EXTRA_RESOURCE, values.get(position));
                        startActivity(intent);
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        addTextChangedListener();

        // ADD ADS:
        AdView mTopAdView = (AdView) findViewById(R.id.main_top_adview);
        AdRequest topAadRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice(getString(R.string.primary_android_admob_test_device))
                .build();
        mTopAdView.loadAd(topAadRequest);

        AdView mBottomAdView = (AdView) findViewById(R.id.main_bottom_adview);
        AdRequest bottomAdRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice(getString(R.string.primary_android_admob_test_device))
                .build();
        mBottomAdView.loadAd(bottomAdRequest);
    }

    /**
     * implement the auto-search functionality by typing in the search field without need for button press.
     */
    public void addTextChangedListener() {
        // get editText component:
        EditText editText = (EditText)findViewById(R.id.findString);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findResource(listView);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // context specific menu items:
        getMenuInflater().inflate(R.menu.menu_add_resource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_email_backup) {
            // warn user that CSV files are not encrypted and may be vulnerable to hackers.
            // if they accept, then go right ahead.
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getText(R.string.unencrypted_risk_dialog_title));
            alertDialog.setMessage(getText(R.string.unencrypted_risk_dialog_message));
            alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));

            alertDialog.setNegativeButton(getText(R.string.unencrypted_risk_dialog_negative_button_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to invoke NO event
                            Log.w(getClass().getName(), getString(R.string.unencrypted_risk_dialog_user_declined_message));
                            Toast.makeText(getApplicationContext(), getString(R.string.unencrypted_risk_dialog_user_declined_message), Toast.LENGTH_SHORT).show();
                        }
                    });

            alertDialog.setPositiveButton(getText(R.string.unencrypted_risk_dialog_positive_button_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.w(getClass().getName(), getString(R.string.unencrypted_risk_dialog_user_accepted_message));
                            onUserSelectedEmailBackupActionSend();
                        }
                    });


            final AlertDialog alert = alertDialog.create();
            alert.show();

            return true;
        }

        if(id == R.id.action_database_backup) {
            this.saveDBEncrypted();
            return true;
        }

        if(id == R.id.action_database_restore) {
            this.loadDBEncryptedChecked();
            return true;
        }

        if(id == R.id.action_change_login) {
            onUserSelectedChangeLogin();
            return true;
        }

        if(id == R.id.action_add_resource) {
            newResource(null);
            return true;
        }

        if(id == R.id.action_show_splash) {
            onShowSplash();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onShowSplash() {
        // create Intent
        Intent intent = new Intent(this, SplashActivity.class);

        // set Intent such that the SplashActivity can check whether it should be in startup mode or "About" mode
        intent.putExtra(SplashActivity.SPLASH_ACTION, SplashActivity.SPLASH_ABOUT);

        // start the activity (user can cxl with back key).
        startActivity(intent);
    }

    /**
     * assumes that os is opened and closed by caller.
     *
     * @param os output stream where data is written
     * @return success or failure code
     * @throws IOException
     */
    public int exportCSV(OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        sb.append("\"_id\",\"ResourceName\",\"UserName\",\"Password\",\"Description\"\n");
        for(Resource r : values) {
            sb.append("\"").append(r.getID()).append("\",");
            sb.append("\"").append(r.getResourceName()).append("\",");
            sb.append("\"").append(r.getUsername()).append("\",");
            sb.append("\"").append(r.getPassword()).append("\",");
            sb.append("\"").append(r.getDescription()).append("\"\n");
            os.write(sb.toString().getBytes());
            sb.setLength(0);
            count++;
        }
        return(count);
    }

    public boolean onUserSelectedExport() {
        int count = 0;
        try {
            FileOutputStream fos = openFileOutput(EXPORT_FILE_NAME, Context.MODE_WORLD_READABLE);  // Context.MODE_PRIVATE prevents email apps from seeing file?
            count = exportCSV(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.export_exported_value_count), count, EXPORT_FILE_NAME), Toast.LENGTH_LONG).show();
        if(count > -1)
            return true;
        else return false;
    }

    /**
     * this method works with both email Android client and gmail, but it does not filter the chooser well at all.
     * @return
     */
    public boolean onUserSelectedEmailBackupActionSend() {
        try {
            onUserSelectedExport();
            Intent emailIntent = new Intent(Intent.ACTION_SEND);  // works with ACTION_SENDTO  in order to filter apps that appear.
            emailIntent.setType("message/rfc822");  // this call was causing an exception within Android, not in this calling code.

            String [] addresses = new String[] { DEFAULT_EXPORT_EMAIL_ADDRESS };  // default for testing.

            if(user_email_address != null && user_email_address.length() > 0) {
                addresses = new String[] { user_email_address };
            } else {
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.export_email_address_missing), logged_in_user), Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), "ERROR: There is no email address for user!  logged_in_user: [" + logged_in_user + "].  USING DEFAULT EMAIL ADDRESS.");
            }

            emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_encrypted_file_subject));
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_encrypted_file_body));

            File attachment = this.getApplicationContext().getFileStreamPath(EXPORT_FILE_NAME);
            if (!attachment.exists() || !attachment.canRead()) {
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.email_encrypted_file_attachment_error), attachment.exists(), attachment.canRead()), Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), String.format(getString(R.string.email_encrypted_file_attachment_error), attachment.exists(), attachment.canRead()));
            } else {
                Uri uri = Uri.fromFile(attachment);
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.email_encrypted_file_attaching), uri.getPath()), Toast.LENGTH_LONG).show();
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            // startActivity(emailIntent);  // ATTACHMENT PERMISSION DENIED IN GMAIL APP, so must use startActivityForResult():
            startActivityForResult(emailIntent, SEND_EMAIL_REQUEST);

        } catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.email_encrypted_file_no_email_clients_configured), Toast.LENGTH_LONG).show();
            return false;
        } catch(Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(getClass().getName(), "inside onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
        switch(requestCode) {
            case LoginActivity.CHANGE_LOGIN:
                if(resultCode == RESULT_OK) {
                    logged_in_user = data.getStringExtra(LoginActivity.LOGGED_IN_USER);
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.logged_in_as_message), logged_in_user), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    logged_in_user = data.getStringExtra(LoginActivity.LOGGED_IN_USER);
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.logged_in_as_message), logged_in_user), Toast.LENGTH_LONG).show();
                } else {
                    // do nothing, I don't know what else could happen?
                }
                break;

            case SEND_EMAIL_REQUEST:
                if(resultCode == RESULT_OK) {
                    Log.i(getClass().getName(), "SUCCESS: " + requestCode + " " + resultCode + " " + data);
                } else {
                    Log.e(getClass().getName(), "UNKNOWN ERROR SENDING EMAIL WITH ATTACHMENT: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
                }
                break;

            default:
                Log.e(getClass().getName(), "ERROR: Received ActivityResult for something we did not request: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
        }
    }

    public void findResource(View view) {
        String findString = ((EditText)findViewById(R.id.findString)).getText().toString();
        adapter.clear();

        for (Resource r : resourcedatasource.findResources(findString)) {
            adapter.add(r);
        }

        // values array is updated via the adapter:
        if (values.isEmpty() && !findString.equals("")) {
            ((EditText)findViewById(R.id.findString)).setTextColor(Color.RED);
            Toast toast = Toast.makeText(getApplicationContext(), String.format(getString(R.string.no_matching_records_found), findString), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            // SET TEXT TO DEFAULT COLOR (saved in onCreate):
            ((EditText)findViewById(R.id.findString)).setTextColor(nDefaultSearchTextColor);
        }
    }

    public void newResource(View view) {
        Intent intent = new Intent(this, NewResourceActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        try {
            resourcedatasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
        findResource(listView);
        super.onResume();
    }

    @Override
    protected void onPause() {
        resourcedatasource.close();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(!((EditText)findViewById(R.id.findString)).getText().toString().isEmpty()) {
            // clear the search string first.
            ((EditText)findViewById(R.id.findString)).setText("");
        } else {
            super.onBackPressed();
        }
    }

    public void onUserSelectedChangeLogin() {
        Log.i(getClass().getName(), "onUserSelectedChangeLogin(): Starting ChangeRegistrationActivity.  logged_in_user: [" + logged_in_user + "]");
        Intent intent = new Intent(this, ChangeRegistrationActivity.class);

        /* Set attributes of Intent to notify RegisterActivity that it should function as
        *  change login activity.  Unset, it will behave as a timeout Splash screen and
        *  then open login when timed out.
        */
        intent.putExtra(LoginActivity.REGISTER_ACTION, LoginActivity.CHANGE_LOGIN);
        intent.putExtra(LoginActivity.LOGGED_IN_USER, logged_in_user);

        // start register activity.  Results are handled in onActivityResult();
        startActivityForResult(intent, LoginActivity.CHANGE_LOGIN);

        // Log results of login change:
        Log.i(getClass().getName(), "onUserSelectedChangeLogin: logged_in_user: [" + logged_in_user + "]");
    }

    /**
     * getDefaultBackupDBFilename
     * @return the fully qualified file name of the default backup file.
     */
    public static String getDefaultBackupDBFilename() {
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/apass/" + ResourceDBHelper.DATABASE_NAME + APP_EXTENSION;
        return(path);
    }

    protected void saveDBEncrypted() {
        // Create FileChooser and register a callback
        FileChooser fileOpenDialog = new FileChooser(
                MainActivity.this,
                "FileSave..",
                new FileChooser.FileChooserListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        // execute when the dialog OK button is pressed.
                        try {
                            AndroidEncryptor.encrypt(ResourceDBHelper.getPath(), chosenDir);
                            Log.e(getClass().getName(), "File " + chosenDir + " Saved Successfully!");
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.file_saved_successfully), chosenDir), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(getClass().getName(), "FILE "+ chosenDir + " SAVE FAILED: ", e);
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.file_save_failed), chosenDir, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // create the default directory if nonexists:
        File defaultBackupDirectory = (new File(getDefaultBackupDBFilename())).getParentFile();
        if(!defaultBackupDirectory.exists()) {
            defaultBackupDirectory.mkdirs();
        }

        // change the default filename using the public variable "default_file_name".
        fileOpenDialog.default_file_name = getDefaultBackupDBFilename();
        fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
    }

    protected void loadDBEncryptedChecked() {
        // warn user that loading data from file will destroy existing data.
        // if they accept, then go right ahead.
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getText(R.string.overwrite_data_dialog_title));
        alertDialog.setMessage(getText(R.string.overwrite_data_dialog_message));
        alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));

        alertDialog.setNegativeButton(getText(R.string.overwrite_data_dialog_negative_button_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        Log.w(getClass().getName(), getString(R.string.overwrite_data_dialog_user_declined_message));
                        Toast.makeText(getApplicationContext(), getString(R.string.overwrite_data_dialog_user_declined_message), Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialog.setPositiveButton(getText(R.string.overwrite_data_dialog_positive_button_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.w(getClass().getName(), getString(R.string.overwrite_data_dialog_user_accepted_message));
                        loadDBEncrypted();
                    }
                });


        final AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected void loadDBEncrypted() {
        // Create FileChooser and register a callback
        FileChooser fileOpenDialog = new FileChooser(
                MainActivity.this,
                "FileOpen..",
                new FileChooser.FileChooserListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        // execute when the dialog OK button is pressed.
                        try {
                            resourcedatasource.close();
                            AndroidEncryptor.decrypt(chosenDir, ResourceDBHelper.getPath());
                            resourcedatasource.open();
                            adapter.notifyDataSetChanged();
                            Log.e(getClass().getName(), "File " + chosenDir + " Loaded Successfully!");
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.file_loaded_successfully), chosenDir), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(getClass().getName(), "FILE " + chosenDir + " LOAD FAILED: ", e);
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.file_load_failed), chosenDir, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // change the default filename using the public variable "default_file_name".
        fileOpenDialog.default_file_name = getDefaultBackupDBFilename();
        fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
    }
}
