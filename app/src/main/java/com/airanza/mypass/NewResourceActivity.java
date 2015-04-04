package com.airanza.mypass;

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

import java.sql.SQLException;

public class NewResourceActivity extends ActionBarActivity {
    public final static String EXTRA_RESOURCE = "com.airanza.mypass.RESOURCE";

    private ResourceDataSource datasource;

    private boolean isPasswordVisible = false;

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }

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
