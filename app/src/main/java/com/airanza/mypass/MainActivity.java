package com.airanza.mypass;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private ResourceDataSource resourcedatasource;
    private LoginDataSource logindatasource;

    private List<Resource> values = null;
    private ListView listView = null;
    private ResourcesAdapter adapter = null;

    static final int LOGIN_REQUEST = 1;
    static final int SEND_EMAIL_REQUEST = 2;
    static final int CHANGE_LOGIN = 3;

    // for saving login state information across Activity lifecycle as needed.
    // there will be a grace period of STAY_LOGGED_IN_MINUTES whereby a user
    // can navigate away from the screen and return if they have logged in during that
    // grace period without entering their username and password again.
    private SharedPreferences prefs = null;
    static final String LOGGED_IN = "logged_in";
    static final String LOGGED_IN_USER = "logged_in_user";
    static final String LOGGED_IN_TIME = "logged_in_time";
    static final String REGISTER_ACTION = "register_action";

    private boolean logged_in = false;
    private String logged_in_user = "";
    private long logged_in_time = 0;
    private final int STAY_LOGGED_IN_MINUTES = 2;

    public final static String EXTRA_RESOURCE = "com.airanza.mypass.RESOURCE";

    private final static String EXPORT_FILE_NAME = "exportCSV.csv";
    // This is retrieved from logindatasource, but if not found, this is the default:
    private final static String DEFAULT_EXPORT_EMAIL_ADDRESS = "crane.edward@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // this is useful for orientation changes:
        if(savedInstanceState != null) {
            logged_in = savedInstanceState.getBoolean(LOGGED_IN);
            logged_in_user = savedInstanceState.getString(LOGGED_IN_USER);
            Log.i(getClass().getName(), "inside onCreate(), savedInstanceState != null, logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "]");
        }

        // this prevents the login screen from appearing for time specified in STAY_LOGGED_IN_MINUTES:
        loadLoginStateFromPreferences();

        // LOGIN HERE if not logged in, or if STAY_LOGGED_IN_MINUTES has elapsed:
        if(!logged_in ||
                !(logged_in_user.length() > 0) || ((System.currentTimeMillis() - logged_in_time) > (STAY_LOGGED_IN_MINUTES * 60 * 1000))) {
            Log.i(getClass().getName(), "Starting Login Activity.  logged_in:[" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST);
            // process the result in this.onActivityResult();
            // if not logged in, we will exit there.
        }

        try {
            resourcedatasource = new ResourceDataSource(this);
            resourcedatasource.open();
            values = resourcedatasource.getAllResources();

            adapter = new ResourcesAdapter(getApplicationContext(), values);
            listView = (ListView)findViewById(R.id.listMain);
            listView.setAdapter(adapter);

            logindatasource = new LoginDataSource(this);
            logindatasource.open();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_export) {
            onUserSelectedExport();
            return true;
        }

        if (id == R.id.action_import_csv) {
            onUserSelectedImportCSV();
            return true;
        }

        if(id == R.id.action_email_backup) {
            onUserSelectedEmailBackupActionSend();
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

    /**
     * TODO: IMPLEMENT CORRECT TOKENIZER LOGIC WITH " delimiters and , separator
     * TODO: ENSURE THAT MULTI-LINE FIELDS WORK PROPERLY
     * @param is
     * @return
     * @throws IOException
     */
    public int importCSV(InputStream is) throws IOException {
        int count = 0;
        String line = "";
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
        fileReader.readLine();  // READ AND IGNORE HEADER:
        while((line = fileReader.readLine()) != null) {
//            String[] tokens = line.split("\",\"");
            String[] tokens = line.split(",(?=([^\"]|\"[^\"]*\")*$)");
            if(tokens.length > 0) {
                // Create a new Resource object and fill its data
                Resource resource = resourcedatasource.createResource(
                tokens[1],
                tokens[2],
                tokens[3],
                tokens[4]
                );

                values.add(resource);
                count++;
            }
        }
        return count;
    }

    public boolean onUserSelectedImportCSV() {
        int count = 0;
        try {
            FileInputStream fis = openFileInput(EXPORT_FILE_NAME);
            count = importCSV(fis);
            fis.close();
        } catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        Toast.makeText(getApplicationContext(), "Imported " + count + " records from " + EXPORT_FILE_NAME, Toast.LENGTH_LONG).show();
        if(count > 0) {
            onResume();
            return true;
        }
        else return false;
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

            // TODO:  Get email address from logged_in_user in logindatasource
            String emailAddress = logindatasource.getEmail(logged_in_user);

            if(emailAddress.length() > 0) {
                addresses = new String[] { emailAddress };
            } else {
                Toast.makeText(getApplicationContext(), "ERROR: There is no logged in user!  logged_in_user: [" + logged_in_user + "].  USING DEFAULT EMAIL ADDRESS.", Toast.LENGTH_LONG).show();
                Log.e(this.getClass().getName(), "ERROR: There is no logged in user!  logged_in_user: [" + logged_in_user + "].  USING DEFAULT EMAIL ADDRESS.");
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
        if(requestCode == LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {

                Log.i(getClass().getName(), "LOGIN SUCCEEDED: " + requestCode + " " + resultCode + " " + data);
                logged_in = true;
                logged_in_user = data.getStringExtra("logged_in_username");
                logged_in_time = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Logged in as [" + logged_in_user + "]", Toast.LENGTH_LONG).show();
            } else {
                Log.e(getClass().getName(), "LOGIN FAILED: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
                logged_in = false;
                logged_in_user = "";
                // Exit this activity, which will also cause program to shut down:
                finish();
            }
        } else if(requestCode == CHANGE_LOGIN) {
            if(resultCode == RESULT_OK) {
                logged_in = true;
                logged_in_user = data.getStringExtra(LOGGED_IN_USER);
                logged_in_time = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Logged in as [" + logged_in_user + "]", Toast.LENGTH_LONG).show();
            } else {
                // THERE IS NOTHING TO DO HERE UNLESS YOU WANT TO LOG USER OFF AND SHUT DOWN.
            }

        } else if(requestCode == SEND_EMAIL_REQUEST) {
            if(resultCode == RESULT_OK) {
                Log.i(getClass().getName(), "SUCCESS: " + requestCode + " " + resultCode + " " + data);
            } else {
                Log.e(getClass().getName(), "UNKNOWN ERROR SENDING EMAIL WITH ATTACHMENT: requestCode = [" + requestCode + "] resultCode = [" + resultCode + "] data: [" + data + "].");
            }
        } else {
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
            logindatasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
        findResource(listView);
        super.onResume();
    }

    @Override
    protected void onPause() {
        resourcedatasource.close();
        logindatasource.close();
        super.onPause();
    }

    public void saveLoginStateToPreferences() {
        prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(LOGGED_IN, logged_in);
        editor.putString(LOGGED_IN_USER, logged_in_user);
        editor.putLong(LOGGED_IN_TIME, logged_in_time);
        editor.commit();
    }

    public void loadLoginStateFromPreferences() {
        prefs = getPreferences(MODE_PRIVATE);
        logged_in = prefs.getBoolean(LOGGED_IN, false);
        logged_in_user = prefs.getString(LOGGED_IN_USER, "");
        logged_in_time = prefs.getLong(LOGGED_IN_TIME, 0);
    }

    public void onStop()
    {
        super.onStop();  // always call the superclass method first
        saveLoginStateToPreferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getClass().getName(), "onDestroy: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
        saveLoginStateToPreferences();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onSaveInstanceState: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
        savedInstanceState.putBoolean(LOGGED_IN, logged_in);
        savedInstanceState.putString(LOGGED_IN_USER, logged_in_user);
        savedInstanceState.putLong(LOGGED_IN_TIME, logged_in_time);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Try to restore the state.  Unfortunately, this seems to be called after onStart(), which
     * is called AFTER onCreate().
     * @param savedInstanceState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logged_in = savedInstanceState.getBoolean(LOGGED_IN);
        logged_in_user = savedInstanceState.getString(LOGGED_IN_USER);
        logged_in_time = savedInstanceState.getLong(LOGGED_IN_TIME);
        Log.i(getClass().getName(), "onRestoreInstanceState: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
    }

    public void onUserSelectedChangeLogin() {
        Log.i(getClass().getName(), "onUserSelectedChangeLogin(): Starting Register Activity.  logged_in:[" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
        Intent intent = new Intent(this, RegisterActivity.class);

        /* TODO:  set attributes of Intent to notify RegisterActivity that it should function as
        *  change login activity
        */
        intent.putExtra(REGISTER_ACTION, CHANGE_LOGIN);
        intent.putExtra(LOGGED_IN_USER, logged_in_user);

        // start register activity.  Results are handled in onActivityResult();
        startActivityForResult(intent, CHANGE_LOGIN);

        // Log results of login change:
        Log.i(getClass().getName(), "onUserSelectedChangeLogin: logged_in: [" + logged_in + "] logged_in_user: [" + logged_in_user + "] logged_in_time: [" + logged_in_time + "]");
    }
}
