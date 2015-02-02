package com.example.ecrane.mypass;

import android.app.ListActivity;
import android.content.Intent;
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

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends ListActivity {

    private ResourceDataSource datasource;

    private List<Resource> values = null;
    private ArrayAdapter<Resource> adapter = null;
    private EditText editText = null;

    public final static String EXTRA_RESOURCE = "com.example.ecrane.mypass.RESOURCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            datasource = new ResourceDataSource(this);
            datasource.open();
            values = datasource.getAllResources();

            adapter = new ArrayAdapter<Resource>(this, android.R.layout.simple_list_item_1, values);
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
        editText = (EditText)findViewById(R.id.findstring);

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

        return super.onOptionsItemSelected(item);
    }

    public void findResource(View view) {
        String findString = ((EditText)findViewById(R.id.findstring)).getText().toString();
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

//    @Override
//    protected void onRestart() {
//        adapter.notifyDataSetChanged();
//        super.onRestart();
//    }
//
    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }
}
