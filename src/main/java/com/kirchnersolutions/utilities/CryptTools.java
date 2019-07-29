package com.kirchnersolutions.utilities;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import static sun.security.krb5.Confounder.bytes;

/**
 *
 * CryptTools Version
 *
 * @author rjojj
 */
public class CryptTools {

    public static final String VERSION = "1.0.02";

    public static Map<String, Object> getRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("private", privateKey);
        keys.put("public", publicKey);
        return keys;
    }

    public static byte[] getPublicKeyBytes(PublicKey publicKey) {
        byte[] keyBytes = publicKey.getEncoded();
        return keyBytes;
    }

    public static String serializePubKey(PublicKey pubKey) {
        RSAPublicKey publicKey = (RSAPublicKey) (pubKey);
        return publicKey.getModulus().toString() + "|"
                + publicKey.getPublicExponent().toString();
    }

    public static PublicKey deserializePubKey(String serial) {
        String[] Parts = serial.split("\\|");
        RSAPublicKeySpec Spec = new RSAPublicKeySpec(
                new BigInteger(Parts[0]),
                new BigInteger(Parts[1]));
        try {
            return KeyFactory.getInstance("RSA").generatePublic(Spec);
        } catch (Exception e) {
            return null;
        }

    }

    public static PublicKey getPublicKeyFromBytes(byte[] key) throws Exception {
        byte[] byteKey = Base64.decodeBase64(new String(key));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        ;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(byteKey);
        PublicKey publicKey = keyFactory.generatePublic(spec);
        return publicKey;
    }

    public static String decryptRSAMsg(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.decodeBase64(encryptedText)));
    }

    public static String encryptRSAMsg(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.encodeBase64String(cipher.doFinal(Base64.decodeBase64(plainText)));
    }



    public static SecretKey generateSecretKey(byte[] password) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digestOfPassword = md.digest(password);
            return new SecretKeySpec(digestOfPassword, "AES");
        } catch (Exception e) {
            return null;
        }
    }
/*
    public static String getRandomSecretAESKeyAsString() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();
        String encodedKey = new String(Base64.encodeBase64(secKey.getEncoded()));
        return encodedKey;
    }

    public static String getSecretAESKeyAsString(byte[] password) throws Exception {
        SecretKey secKey = generateSecretKey(password);
        String encodedKey = new String(Base64.encodeBase64(secKey.getEncoded()));
        return encodedKey;
    }

    public static String encryptTextUsingAES(String plainText, String aesKeyString) throws Exception {
        byte[] decodedKey = Base64.decodeBase64(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, originalKey);
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
        return new String(Base64.encodeBase64(byteCipherText));
    }

    public static String decryptTextUsingAES(String encryptedText, String aesKeyString) throws Exception {
        byte[] decodedKey = Base64.decodeBase64(aesKeyString);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] bytePlainText = aesCipher.doFinal(Base64.decodeBase64(encryptedText));
        return new String(bytePlainText);
    }

 */

    public static byte[] getSHA256(String msg) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());
        byte[] byteData = md.digest();
        return byteData;
    }
}
