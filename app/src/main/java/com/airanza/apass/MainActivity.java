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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends ActionBarActivity {

    private ResourceDataSource resourcedatasource;

    private List<Resource> values = null;
    private ListView listView = null;
    private ResourcesAdapter adapter = null;

    static final int SEND_EMAIL_REQUEST = 2;

    private String logged_in_user = "";
    private long logged_in_time = 0;

    public final static String EXTRA_RESOURCE = "com.airanza.apass.RESOURCE";

    private final static String EXPORT_FILE_NAME = "exportCSV.csv";

    public final static String APP_EXTENSION = ".apa";  // extension for exported, encrypted files.
    private final static String ENCRYPTION_KEY = "APassApp";

    // This is retrieved from logindatasource, but if not found, this is the default:
    private final static String DEFAULT_EXPORT_EMAIL_ADDRESS = "crane.edward@gmail.com";
    private static String user_email_address = DEFAULT_EXPORT_EMAIL_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logged_in_user = getIntent().getStringExtra(LoginActivity.LOGGED_IN_USER);
        user_email_address = getIntent().getStringExtra(LoginActivity.USER_EMAIL_ADDRESS);
        logged_in_time = getIntent().getLongExtra(LoginActivity.LOGGED_IN_TIME, 0);

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
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_email_backup) {
            onUserSelectedEmailBackupActionSend();
            return true;
        }

        if(id == R.id.action_database_backup) {
            this.exportDBToSDEncrypted();
            return true;
        }

        if(id == R.id.action_database_restore) {
            this.importDBFromSDEncrypted();
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

        return super.onOptionsItemSelected(item);
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
        Toast.makeText(getApplicationContext(), "Exported " + count + " records to " + EXPORT_FILE_NAME, Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "ERROR: There is no email address for user!  logged_in_user: [" + logged_in_user + "].  USING DEFAULT EMAIL ADDRESS.", Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), "ERROR: There is no email address for user!  logged_in_user: [" + logged_in_user + "].  USING DEFAULT EMAIL ADDRESS.");
            }

            emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyPass Backup File");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Backup of Entries in MyPass.");

            File attachment = this.getApplicationContext().getFileStreamPath(EXPORT_FILE_NAME);
            if (!attachment.exists() || !attachment.canRead()) {
                Toast.makeText(getApplicationContext(), "ATTACHMENT ERROR!  Exists: [" + attachment.exists() + "] canRead: [" + attachment.canRead() + "].", Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), "ATTACHMENT ERROR!  Exists: [" + attachment.exists() + "] canRead: [" + attachment.canRead() + "].");
            } else {
                Uri uri = Uri.fromFile(attachment);
                Toast.makeText(getApplicationContext(), "ATTACHING: " + uri.getPath(), Toast.LENGTH_LONG).show();
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            // startActivity(emailIntent);  // ATTACHMENT PERMISSION DENIED IN GMAIL APP, so must use startActivityForResult():
            startActivityForResult(emailIntent, SEND_EMAIL_REQUEST);

        } catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Logged in as [" + logged_in_user + "]", Toast.LENGTH_LONG).show();
                } else {
                    // THERE IS NOTHING TO DO HERE UNLESS YOU WANT TO LOG USER OFF AND SHUT DOWN.
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
        System.out.println("Searching [" + findString + "].");
        adapter.clear();

        for (Resource r : resourcedatasource.findResources(findString)) {
            adapter.add(r);
        }

        // values array is updated via the adapter:
        if (values.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Records matching \"" + findString + "\" were found", Toast.LENGTH_LONG).show();
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

    public void onStop()
    {
        super.onStop();  // always call the superclass method first
    }

    public void onUserSelectedChangeLogin() {
        Log.i(getClass().getName(), "onUserSelectedChangeLogin(): Starting Register Activity.  logged_in_user: [" + logged_in_user + "]");
        Intent intent = new Intent(this, RegisterActivity.class);

        /* TODO:  set attributes of Intent to notify RegisterActivity that it should function as
        *  change login activity
        */
        intent.putExtra(LoginActivity.REGISTER_ACTION, LoginActivity.CHANGE_LOGIN);
        intent.putExtra(LoginActivity.LOGGED_IN_USER, logged_in_user);

        // start register activity.  Results are handled in onActivityResult();
        startActivityForResult(intent, LoginActivity.CHANGE_LOGIN);

        // Log results of login change:
        Log.i(getClass().getName(), "onUserSelectedChangeLogin: logged_in_user: [" + logged_in_user + "]");
    }

    private void copyDBToSD() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if(sd.canWrite()) {
                String currentDBPath = "//data//" + "com.airanza.apass" + "//databases//" + ResourceDBHelper.DATABASE_NAME;
                String backupDBDir = "//apass//";
                String backupDBPath = backupDBDir + ResourceDBHelper.DATABASE_NAME;

                File currentDB = new File(data, currentDBPath);

                // create dir if nonexists:
                File backupDBDirFile = new File(sd, backupDBDir);
                backupDBDirFile.mkdirs();

                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getApplicationContext(), "Backup Successful!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cannot write to sd: " + sd, Toast.LENGTH_LONG).show();
                Log.i(getClass().getName(), "Cannot write to sd directory: " + sd);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "BACKUP FAILED: ", e );
            Toast.makeText(getApplicationContext(), "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportDBToSDEncrypted() {
        byte k[] = ENCRYPTION_KEY.getBytes();
        SecretKeySpec key = new SecretKeySpec(k, "DES/ECB/PKCS5Padding".split("/")[0]);

        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if(sd.canWrite()) {
                String currentDBPath = "//data//" + "com.airanza.apass" + "//databases//" + ResourceDBHelper.DATABASE_NAME;
                String backupDBDir = "//apass//";
                String backupDBPath = backupDBDir + ResourceDBHelper.DATABASE_NAME + APP_EXTENSION;

                File currentDB = new File(data, currentDBPath);

                // create dir if nonexists:
                File backupDBDirFile = new File(sd, backupDBDir);
                backupDBDirFile.mkdirs();

                File backupDB = new File(sd, backupDBPath);

                FileInputStream fIn = new FileInputStream(currentDB);
                FileOutputStream fOut = new FileOutputStream(backupDB);

                CipherOutputStream cOut = new CipherOutputStream(fOut, cipher);

                byte[] buffer = new byte[8192];
                int length;
                while((length = fIn.read(buffer)) != -1)
                    cOut.write(buffer, 0, length);
                fIn.close();
                cOut.close();

                Toast.makeText(getApplicationContext(), "Backup Successful!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cannot write to sd: " + sd, Toast.LENGTH_LONG).show();
                Log.i(getClass().getName(), "Cannot write to sd directory: " + sd);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "BACKUP FAILED: ", e );
            Toast.makeText(getApplicationContext(), "Backup Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * importDBFromSD does not work yet, as the data directory cannot be written to.  Further research.
     */
    private void copyDBFromSD(){
        resourcedatasource.close();
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            String appDBPath = "//data//" + "com.airanza.apass" + "//databases//" + ResourceDBHelper.DATABASE_NAME;
            String backupDBDir = "//apass//";
            String backupDBPath = backupDBDir + ResourceDBHelper.DATABASE_NAME;

            File appDB = new File(data, appDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileChannel src = new FileInputStream(backupDB).getChannel();
            FileChannel dst = new FileOutputStream(appDB).getChannel();

            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "DB Imported!", Toast.LENGTH_LONG).show();
            Log.i(getClass().getName(), "DB Import Successful");

            resourcedatasource.open();
        } catch(IOException e) {
            Toast.makeText(this, "DB IMPORT FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(getClass().getName(), "DB IMPORT FAILED", e);
        } catch(SQLException e) {
            Toast.makeText(this, "DB IMPORT FAILED - CANNOT OPEN DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(getClass().getName(), "DB IMPORT FAILED - CANNOT OPEN DB", e);
        }
    }

    /**
     * importDBFromSD does not work yet, as the data directory cannot be written to.  Further research.
     */
    private void importDBFromSDEncrypted(){
        byte k[] = this.ENCRYPTION_KEY.getBytes();
        SecretKeySpec key = new SecretKeySpec(k, "DES/ECB/PKCS5Padding".split("/")[0]);

        resourcedatasource.close();

        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            String appDBPath = "//data//" + "com.airanza.apass" + "//databases//" + ResourceDBHelper.DATABASE_NAME;
            String backupDBDir = "//apass//";
            String backupDBPath = backupDBDir + ResourceDBHelper.DATABASE_NAME + APP_EXTENSION;

            File appDB = new File(data, appDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileInputStream in = new FileInputStream(backupDB);
            FileOutputStream fOut = new FileOutputStream(appDB);
            CipherOutputStream cOut = new CipherOutputStream(fOut, cipher);

            byte[] buffer = new byte[8192];
            int length;
            while((length = in.read(buffer)) != -1)
                cOut.write(buffer, 0, length);
            in.close();
            cOut.close();

            resourcedatasource.open();

            Toast.makeText(this, "DB Imported!", Toast.LENGTH_LONG).show();
            Log.i(getClass().getName(), "DB Import Successful");
        } catch(IOException e) {
            Toast.makeText(this, "DB IMPORT FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(getClass().getName(), "DB IMPORT FAILED", e);
        } catch(SQLException e) {
            Toast.makeText(this, "DB IMPORT FAILED - CANNOT OPEN DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(getClass().getName(), "DB IMPORT FAILED - CANNOT OPEN DB", e);
        } catch(Exception e) {
            Toast.makeText(this, "DB IMPORT FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(getClass().getName(), "DB IMPORT FAILED", e);
        }
    }
}
