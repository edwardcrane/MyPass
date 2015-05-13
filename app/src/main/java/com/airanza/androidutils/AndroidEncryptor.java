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

import android.util.Log;

import com.airanza.utils.FileEncryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;

/**
 * Created by ecrane on 5/8/2015.
 */
public class AndroidEncryptor {

    public static void decrypt(String source, String destination) throws Exception {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destinationFile);

        FileEncryptor.cryptStream(Cipher.DECRYPT_MODE, in, out);

        in.close();
        out.close();
        Log.i("AndroidEncryptor", "decrypt " + source + " -> " + destination + " Successful.");
    }

    public static void encrypt(String source, String destination) throws Exception {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        destinationFile.getParentFile().mkdirs();

        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destinationFile);

        FileEncryptor.cryptStream(Cipher.ENCRYPT_MODE, in, out);

        in.close();
        out.close();
        Log.i("AndroidEncryptor", "encrypt " + source + " -> " + destination + " Successful.");
    }
}
