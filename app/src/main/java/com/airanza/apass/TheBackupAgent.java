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

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

/**
 * TheBackupAgent uses Google's backup api to backup app data in case the app is
 * installed on a new phone, etc.
 *
 * TheBackupAgent is driven by Android Training located at:
 * http://developer.android.com/training/cloudsync/backupapi.html
 *
 * This is for general data typically <1MB in size.
 *
 * This can also backup Preferences by using SharedPreferencesBackupHelper
 * should such preferences become utilized later.
 */
public class TheBackupAgent extends BackupAgentHelper {
    // A key to uniquely identify the set of backup data
    static final String APASS_FILES_BACKUP_KEY = "apassfiles";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        FileBackupHelper fileBackupHelper = new FileBackupHelper(this,
                ResourceDBHelper.getEncryptedBackupPath(),
                LoginDBHelper.getEncryptedBackupPath());

        addHelper(APASS_FILES_BACKUP_KEY, fileBackupHelper);
    }
}