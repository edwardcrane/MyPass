package com.airanza.mypass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecrane on 3/7/2015.
 */
public class ResourcesAdapter extends ArrayAdapter<Resource> {
    public ResourcesAdapter(Context context, List<Resource> resources) {
        super(context, 0, resources);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get the data item for this position
        Resource resource = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.resourcerowlayout, parent, false);
        }
        // Lookup view for data population
        TextView text1 = (TextView) convertView.findViewById(R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(R.id.text2);
        // Populate the data into the template view using the data object
        text1.setText(resource.getResourceName());
        text2.setText(resource.getDescription());

        // return the completed view to render on screen
        return convertView;
    }
}
