package com.github.onsdigital.zebedee;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Hasher {

    public static void main(String[] args) throws Exception {
        apacheMD5();
        manualMD5();
    }

    static void apacheMD5() throws Exception {
        try (InputStream in = new FileInputStream("/Users/dave/desktop/guitars.cypher")) {
            System.out.println("apache:");
            System.out.println(DigestUtils.md5Hex(in));
        }
    }


    static void manualMD5() throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (
                InputStream in = new FileInputStream("/Users/dave/desktop/guitars.cypher");
                DigestInputStream dis = new DigestInputStream(in, md)
        ) {

            DigestUtils.sha1Hex(in);


            while (dis.read() != -1) {
                md = dis.getMessageDigest();
            }
        }

        System.out.println("manual:");
        System.out.println(Hex.encodeHex(md.digest()));
    }
}
