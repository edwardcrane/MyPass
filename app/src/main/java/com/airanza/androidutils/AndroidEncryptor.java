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

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.airanza.utils.FileEncryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ecrane on 5/8/2015.
 */
public class AndroidEncryptor {
    /**
     * importDBFromSD
     * TODO:  Must implement File Chooser so we can select importable file.
     */
    public static void importDBFromSDEncrypted(String source, String destination) throws Exception {
        byte k[] = FileEncryptor.ENCRYPTION_KEY.getBytes();
        SecretKeySpec key = new SecretKeySpec(k, FileEncryptor.ENCRYPTION_ALGO.split("/")[0]);

        Cipher cipher = Cipher.getInstance(FileEncryptor.ENCRYPTION_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key);

        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        File appDB = new File(data, destination);
        File backupDB = new File(sd, source);

        FileInputStream in = new FileInputStream(backupDB);
        FileOutputStream fOut = new FileOutputStream(appDB);
        CipherOutputStream cOut = new CipherOutputStream(fOut, cipher);

        byte[] buffer = new byte[FileEncryptor.BUFFER_SIZE];
        int length;
        while((length = in.read(buffer)) != -1)
            cOut.write(buffer, 0, length);
        in.close();
        cOut.close();

        Log.i("FileEncryptor", "DB Import Successful");
    }

    public static void exportDBToSDEncrypted(String source, String destination) throws Exception {
        byte k[] = FileEncryptor.ENCRYPTION_KEY.getBytes();
        SecretKeySpec key = new SecretKeySpec(k, FileEncryptor.ENCRYPTION_ALGO.split("/")[0]);

        Cipher cipher = Cipher.getInstance(FileEncryptor.ENCRYPTION_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if(sd.canWrite()) {
            File currentDB = new File(data, source);

            // create dir if nonexists:
            File backupDBDirFile = new File(sd, destination);
            backupDBDirFile.mkdirs();

            File backupDB = new File(sd, destination);

            FileInputStream fIn = new FileInputStream(currentDB);
            FileOutputStream fOut = new FileOutputStream(backupDB);

            CipherOutputStream cOut = new CipherOutputStream(fOut, cipher);

            byte[] buffer = new byte[FileEncryptor.BUFFER_SIZE];
            int length;
            while((length = fIn.read(buffer)) != -1)
                cOut.write(buffer, 0, length);
            fIn.close();
            cOut.close();
        } else {
            Toast.makeText(null, "Cannot write to sd: " + sd, Toast.LENGTH_LONG).show();
            throw new IOException("Cannot write to SD directory: " + sd);
        }
    }
}
