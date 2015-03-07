package com.airanza.mypass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

// TODO: modify confirmation box, to use a special message if the text has been modified.
// TODO:  Figure out a way to make the Toast strings portable across languages (such as a string with %s embedded) subbing in resource

public class EditResourceActivity extends ActionBarActivity {
    public final static String EXTRA_RESOURCE = "com.airanza.mypass.RESOURCE";

    private ResourceDataSource datasource;
    private Resource resource;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        resource = (Resource)intent.getSerializableExtra(EXTRA_RESOURCE);

        setContentView(R.layout.activity_edit_resource);
        try {
            datasource = new ResourceDataSource(this);
            datasource.open();
        } catch (SQLException e) {
            Log.w(this.getClass().getName(), e);
        }
        ((TextView)findViewById(R.id._id)).setText("" + resource.getID());
        ((EditText)findViewById(R.id.resource_name)).setText(resource.getResourceName());
        ((EditText)findViewById(R.id.username)).setText(resource.getUsername());
        ((EditText)findViewById(R.id.password)).setText(resource.getPassword());
        ((EditText)findViewById(R.id.description)).setText(resource.getDescription());

        final EditText editTextPassword = (EditText)findViewById(R.id.password);

        editTextPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPasswordVisible) {  // show password
                    editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {                  // Hide password
                    editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                editTextPassword.setSelection(editTextPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
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

        return super.onOptionsItemSelected(item);
    }

    public void saveEntry(View view) {
                resource.setResourceName(((EditText)findViewById(R.id.resource_name)).getText().toString());
                resource.setUsername(((EditText)findViewById(R.id.username)).getText().toString());
                resource.setPassword(((EditText)findViewById(R.id.password)).getText().toString());
                resource.setDescription(((EditText)findViewById(R.id.description)).getText().toString());

        datasource.update(resource);

        Toast.makeText(getApplicationContext(), "Updated Resource [" + resource + "]", Toast.LENGTH_LONG).show();
        Log.w(this.getClass().getName(), "Updated Resource [" + resource + "]");

        finish();
    }

    public void deleteResource(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getText(R.string.delete_resource_confirm_title));
        alertDialog.setMessage(getText(R.string.delete_resource_confirm_message));
        alertDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));

        alertDialog.setNegativeButton(getText(R.string.delete_resource_dialog_negative_button_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        Toast.makeText(getApplicationContext(), "Resource [" + resource + "] NOT DELETED.", Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialog.setPositiveButton(getText(R.string.delete_resource_dialog_positive_button_text),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.w(this.getClass().getName(), "Deleting resource [" + resource + "]");
                        datasource.deleteResource(resource);
                        Toast.makeText(getApplicationContext(), "Deleted Resource [" + resource + "]", Toast.LENGTH_LONG).show();

                        finish();
                    }
                });


        final AlertDialog alert = alertDialog.create();
        alert.show();
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