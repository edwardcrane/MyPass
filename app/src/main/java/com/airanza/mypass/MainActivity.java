package com.airanza.mypass;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class MainActivity extends ListActivity {

    private ResourceDataSource datasource;

    private List<Resource> values = null;
    private ArrayAdapter<Resource> adapter = null;
    private EditText editText = null;

    static final int SEND_EMAIL_REQUEST = 1;

    public final static String EXTRA_RESOURCE = "com.airanza.mypass.RESOURCE";

    private final static String EXPORT_FILE_NAME = "exportCSV.csv";

    // TODO: Store this somewhere manageable through settings:
    private final static String DEFAULT_EXPORT_EMAIL_ADDRESS = "crane.edward@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            datasource = new ResourceDataSource(this);
            datasource.open();
            values = datasource.getAllResources();

//            adapter = new ArrayAdapter<Resource>(this, android.R.layout.simple_list_item_1, values);
            adapter = new ArrayAdapter<Resource>(this, R.layout.rowlayout, R.id.label, values);
            Log.i(getClass().getName(), "About to call setListAdapter(" + adapter + ") with " + R.layout.rowlayout + ";");
            setListAdapter(adapter);
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }

        ListView listView = (ListView)findViewById(android.R.id.list);
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
        editText = (EditText)findViewById(R.id.findString);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                findResource(getListView());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * assumes that os is opened and closed by caller.
     *
     * @param os
     * @return
     * @throws IOException
     */
    public int exportCSV(OutputStream os) throws IOException {
        StringBuffer sb = new StringBuffer();
        int count = 0;

        sb.append("\"_id\",\"ResourceName\",\"UserName\",\"Password\",\"Description\"\n");
        for(Resource r : values) {
            sb.append("\"" + r.getID() + "\",");
            sb.append("\"" + r.getResourceName() + "\",");
            sb.append("\"" + r.getUsername() + "\",");
            sb.append("\"" + r.getPassword() + "\",");
            sb.append("\"" + r.getDescription() + "\"\n");
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
                Resource resource = datasource.createResource(
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

//    /**
//     * Sadly, this method using SENDTO only works with gmail, but it filters the chooser better.
//     * @return
//     */
//    public boolean onUserSelectedEmailBackupActionSendTo() {
//        try {
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);  // works with ACTION_SENDTO  in order to filter apps that appear.
//            // emailIntent.setType("*/*");  // this call was causing an exception within Android, not in this calling code.
//            emailIntent.setData(Uri.parse("mailto:")); // only email apps will handle this.
//
//            String [] addresses = new String[] { DEFAULT_EXPORT_EMAIL_ADDRESS };  // default for testing.
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
//
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyPass Backup File");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Backup of Entries in MyPass.");
//
//            File attachment = this.getApplicationContext().getFileStreamPath(EXPORT_FILE_NAME);
//            if (!attachment.exists() || !attachment.canRead()) {
//                Toast.makeText(getApplicationContext(), "ATTACHMENT ERROR!  Exists: [" + attachment.exists() + "] canRead: [" + attachment.canRead() + "].", Toast.LENGTH_LONG).show();
//                Log.e(this.getClass().getName(), "ATTACHMENT ERROR!  Exists: [" + attachment.exists() + "] canRead: [" + attachment.canRead() + "].");
//            } else {
//                Uri uri = Uri.fromFile(attachment);
//                Toast.makeText(getApplicationContext(), uri.getPath(), Toast.LENGTH_LONG).show();
//                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
//            }
//
//            startActivityForResult(emailIntent, SEND_EMAIL_REQUEST);
//
//        } catch(android.content.ActivityNotFoundException ex) {
//            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
//            return false;
//        } catch(Exception e) {
//            Log.e(getClass().getName(), e.getMessage(), e);
//        }
//        return true;
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SEND_EMAIL_REQUEST) {
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

        values = datasource.findResources(findString);
        if (values.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Records matching \"" + findString + "\" were found", Toast.LENGTH_LONG).show();
        }

        // TODO: Consider overriding hasStableIds() to help ListView.
        adapter = new ArrayAdapter<Resource>(this, android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    public void newResource(View view) {
        Intent intent = new Intent(this, NewResourceActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        try {
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
        findResource(getListView());
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }
}
