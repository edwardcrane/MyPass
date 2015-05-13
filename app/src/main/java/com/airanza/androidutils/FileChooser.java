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

package com.airanza.androidutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ecrane on 5/7/2015.
 */
public class FileChooser {
    private static final int FileOpen     = 0;
    private static final int FileSave     = 1;
    private static final int FolderChoose = 2;
    private int Select_type = FileSave;
    private String m_sdcardDirectory = "";
    private Context m_context;
    private TextView m_titleView1;
    private TextView m_titleView;
    public String default_file_name = "default.txt";
    private String selected_file_name = default_file_name;
    private EditText input_text;

    private String m_dir = "";
    private List<String> m_subdirs = null;
    private FileChooserListener m_FileChooserListener = null;
    private ArrayAdapter<String> m_listAdapter = null;
    private boolean m_goToUpper = false;

    /**
     * FileChooserListener is callback interface for selected directory
     */
    public interface FileChooserListener
    {
        public void onChosenDir(String chosenDir);
    }

    public FileChooser(Context context, String file_select_type, FileChooserListener FileChooserListener)
    {
        if (file_select_type.equals("FileOpen"))             {Select_type = FileOpen;}
        else if (file_select_type.equals("FileSave"))       {Select_type = FileSave;}
        else if (file_select_type.equals("FolderChoose"))   {Select_type = FolderChoose;}
        else if (file_select_type.equals("FileOpen.."))     {Select_type = FileOpen; m_goToUpper = true;}
        else if (file_select_type.equals("FileSave.."))     {Select_type = FileSave; m_goToUpper = true;}
        else if (file_select_type.equals("FolderChoose..")) {Select_type = FolderChoose; m_goToUpper = true;}
        else Select_type = FileOpen;

        m_context = context;
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_FileChooserListener = FileChooserListener;

        try
        {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        }
        catch (IOException ioe)
        {
        }
    }

    /**
     * chooseFile_or_Dir() - load directory chooser dialog for initial
     * default sdcard directory
     */
    public void chooseFile_or_Dir()
    {
        // Initial directory is sdcard directory
        if (m_dir.equals(""))	chooseFile_or_Dir(m_sdcardDirectory);
        else chooseFile_or_Dir(m_dir);
    }

    /**
     * chooseFile_or_Dir(String dir) - load directory chooser dialog for initial
     * input 'dir' directory
     * @param dir
     */
    public void chooseFile_or_Dir(String dir)
    {
        File dirFile = new File(dir);
        while (! dirFile.exists() || ! dirFile.isDirectory())
        {
            dir = dirFile.getParent();
            dirFile = new File(dir);
            Log.d("~~~~~","dir="+dir);
        }
        Log.d("~~~~~", "dir=" + dir);
        //m_sdcardDirectory
        try
        {
            dir = new File(dir).getCanonicalPath();
        }
        catch (IOException ioe)
        {
            return;
        }

        m_dir = dir;
        m_subdirs = getDirectories(dir);

        class FileChooserOnClickListener implements DialogInterface.OnClickListener
        {
            public void onClick(DialogInterface dialog, int item)
            {
                String m_dir_old = m_dir;
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                if (sel.charAt(sel.length()-1) == '/')	sel = sel.substring(0, sel.length()-1);

                // Navigate into the sub-directory
                if (sel.equals(".."))
                {
                    m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
                    if("".equals(m_dir)) {
                        m_dir = "/";
                    }
                }
                else
                {
                    m_dir += "/" + sel;
                }
                selected_file_name = default_file_name;

                if ((new File(m_dir).isFile())) // If the selection is a regular file
                {
                    m_dir = m_dir_old;
                    selected_file_name = sel;
                }

                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, m_subdirs,
                new FileChooserOnClickListener());

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Current directory chosen
                // Call registered listener supplied with the chosen directory
                if (m_FileChooserListener != null){
                    {
                        if (Select_type == FileOpen || Select_type == FileSave) {
                            selected_file_name = input_text.getText() + "";
                            File f = new File(selected_file_name);
                            m_FileChooserListener.onChosenDir(m_dir + File.separatorChar + f.getName());
                        }
                        else
                        {
                            m_FileChooserListener.onChosenDir(m_dir);
                        }
                    }
                }
            }
        }).setNegativeButton("Cancel", null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        // Show directory chooser dialog
        dirsDialog.show();
    }

    private boolean createSubDir(String newDir)
    {
        File newDirFile = new File(newDir);
        if   (! newDirFile.exists() ) return newDirFile.mkdir();
        else return false;
    }

    private List<String> getDirectories(String dir)
    {
        List<String> dirs = new ArrayList<String>();
        try
        {
            File dirFile = new File(dir);

            // if directory is not the base sd card directory add ".." for going up one directory
            if ((m_goToUpper || ! m_dir.equals(m_sdcardDirectory) )
                    && !"/".equals(m_dir)
                    ) {
                dirs.add("..");
            }
            Log.d("~~~~","m_dir="+m_dir);
            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
                return dirs;
            }

            for (File file : dirFile.listFiles())
            {
                if ( file.isDirectory())
                {
                    // Add "/" to directory names to identify them in the list
                    dirs.add( file.getName() + "/" );
                }
                else if (Select_type == FileSave || Select_type == FileOpen)
                {
                    // Add file names to the list if we are doing a file save or file open operation
                    dirs.add( file.getName() );
                }
            }
        }
        catch (Exception e)	{}

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        return dirs;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////                                   START DIALOG DEFINITION                                    //////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);
        ////////////////////////////////////////////////
        // Create title text showing file select type //
        ////////////////////////////////////////////////
        m_titleView1 = new TextView(m_context);
        m_titleView1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView1.setTextAppearance(m_context, android.R.style.TextAppearance_Large);
        //m_titleView1.setTextColor( m_context.getResources().getColor(android.R.color.black) );

        if (Select_type == FileOpen    ) m_titleView1.setText("Open:");
        if (Select_type == FileSave    ) m_titleView1.setText("Save As:");
        if (Select_type == FolderChoose) m_titleView1.setText("Folder Select:");

        //need to make this a variable Save as, Open, Select Directory
        m_titleView1.setGravity(Gravity.CENTER_VERTICAL);
        m_titleView1.setBackgroundColor(-12303292); // dark gray 	-12303292
        m_titleView1.setTextColor( m_context.getResources().getColor(android.R.color.white) );

        // Create custom view for AlertDialog title
        LinearLayout titleLayout1 = new LinearLayout(m_context);
        titleLayout1.setOrientation(LinearLayout.VERTICAL);
        titleLayout1.addView(m_titleView1);


        if (Select_type == FolderChoose || Select_type == FileSave)
        {
            ///////////////////////////////
            // Create New Folder Button  //
            ///////////////////////////////
            Button newDirButton = new Button(m_context);
            newDirButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            newDirButton.setText("New Folder");
            newDirButton.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    final EditText input = new EditText(m_context);

                                                    // Show new folder name input dialog
                                                    new AlertDialog.Builder(m_context).
                                                            setTitle("New Folder Name").
                                                            setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                    {
                                                        public void onClick(DialogInterface dialog, int whichButton)
                                                        {
                                                            Editable newDir = input.getText();
                                                            String newDirName = newDir.toString();
                                                            // Create new directory
                                                            if ( createSubDir(m_dir + "/" + newDirName) )
                                                            {
                                                                // Navigate into the new directory
                                                                m_dir += "/" + newDirName;
                                                                updateDirectory();
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(m_context, "Failed to create '"
                                                                        + newDirName + "' folder", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }).setNegativeButton("Cancel", null).show();
                                                }
                                            }
            );
            titleLayout1.addView(newDirButton);
        }

        // Create View with folder path and entry text box //
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView.setBackgroundColor(-12303292); // dark gray -12303292
        m_titleView.setTextColor( m_context.getResources().getColor(android.R.color.white) );
        m_titleView.setGravity(Gravity.CENTER_VERTICAL);
        m_titleView.setText(title);

        titleLayout.addView(m_titleView);

        if (Select_type == FileOpen || Select_type == FileSave)
        {
            input_text = new EditText(m_context);
            input_text.setText(default_file_name);
            titleLayout.addView(input_text);
        }
        //////////////////////////////////////////
        // Set Views and Finish Dialog builder  //
        //////////////////////////////////////////
        dialogBuilder.setView(titleLayout);
        dialogBuilder.setCustomTitle(titleLayout1);
        m_listAdapter = createListAdapter(listItems);
        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);
        return dialogBuilder;
    }

    private void updateDirectory()
    {
        m_subdirs.clear();
        m_subdirs.addAll( getDirectories(m_dir) );
        m_titleView.setText(m_dir);
        m_listAdapter.notifyDataSetChanged();
        //#scorch
        if (Select_type == FileSave || Select_type == FileOpen)
        {
            input_text.setText(selected_file_name);
        }
    }

    private ArrayAdapter<String> createListAdapter(List<String> items)
    {
        return new ArrayAdapter<String>(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView)
                {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }

}
