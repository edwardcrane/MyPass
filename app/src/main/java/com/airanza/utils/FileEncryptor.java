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

package com.airanza.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ecrane on 4/20/2015.
 */
public class FileEncryptor {

    public final static int BUFFER_SIZE = 8192;
    public final static String ENCRYPTION_ALGO = "DES/ECB/PKCS5Padding";
    public final static String ENCRYPTION_KEY = "APassApp";

    private static SecretKeySpec getKey() {
        byte k[] = ENCRYPTION_KEY.getBytes();
        return(new SecretKeySpec(k, ENCRYPTION_ALGO.split("/")[0]));
    }

    /**
     * encryptFile
     *
     * @param infile file to be encrypted.
     * @param outfile encrypted output file.
     * @throws Exception
     */
    public static void encryptFile(String infile, String outfile)
            throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());

        FileInputStream in = new FileInputStream(infile);
        FileOutputStream fileOut = new FileOutputStream(outfile);

        CipherOutputStream out = new CipherOutputStream(fileOut, cipher);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);
        in.close();
        out.close();
    }

    /**
     * decryptFile
     *
     * @param infile encrypted file to be decrypted.
     * @param outfile decrypted file.
     * @throws Exception
     */
    public static void decryptFile(String infile, String outfile)
            throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, getKey());

        FileInputStream in = new FileInputStream(infile);
        FileOutputStream fileOut = new FileOutputStream(outfile);

        CipherOutputStream out = new CipherOutputStream(fileOut, cipher);
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);
        in.close();
        out.close();
    }

    public static void main(String args[]) {
        switch(args[0]) {
            case "-e":
                try {
                    encryptFile(args[1], args[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "-d":
                try {
                    decryptFile(args[1], args[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                usage();
        }
    }

    public static void usage() {
        System.out.println("USAGE:  java FileEncryptor -e | -d inputfile outputfile");
    }
}
