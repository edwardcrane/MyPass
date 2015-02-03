package com.example.ecrane.mypass;

import android.text.Html;

import java.io.Serializable;

/**
 * Created by ecrane on 1/19/2015.
 */
public class Resource implements Serializable {
    private long _id = 0;
    private String entryID = "";
    private String resourceName = "";
    private String username = "";
    private String password = "";
    private String description = "";

    public long getID() {
        return _id;
    }

    public void setID(long id) {
        _id = id;
    }

    public String getEntryID() {
        return entryID;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() { return toDisplayString(); }

    public String toDisplayString() {
        StringBuffer buf = new StringBuffer();
        buf.append(resourceName);
        if(!description.isEmpty()) {
            buf.append("\n[" + description + "]");
        };
        return(buf.toString());
    }

    public String toShortString() {
        return(resourceName);
    }

    public String toLongString(){
        StringBuffer buf = new StringBuffer();
        buf.append("[" + _id + "] ");
        buf.append("[" + entryID + "] ");
        buf.append("[" + resourceName + "] ");
        buf.append("[" + username + "] ");
        buf.append("[" + password + "] ");
        buf.append("[" + description + "]");

        return(buf.toString());
    }
}
