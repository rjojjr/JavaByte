package com.kirchnersolutions.database.Servers.socket;

import com.kirchnersolutions.utilities.CryptTools;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

public class Keys {

    private Map<String, Object> RSAKeys;
    private SecretKey secretKey = null;
    private PublicKey publicKey = null;

    void generateRSAKeys() throws Exception{
        RSAKeys = CryptTools.getRSAKeys();
    }

    boolean getPublicKey(String key){
        publicKey = CryptTools.deserializePubKey(key);
        if(publicKey == null){
            return false;
        }
        return true;
    }

    String encryptAESKey() {
        if (publicKey == null) {
            return null;
        }
        try{
            secretKey = CryptTools.generateRandomSecretKey();
            return CryptTools.encryptRSAMsg(new String(CryptTools.serializeAESKey(secretKey), "UTF-8"), publicKey);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    byte[] decryptAESResponse(byte[] response) throws Exception{
        if(secretKey == null){
            return null;
        }
        return CryptTools.aesDecrypt(secretKey, Base64.getDecoder().decode(response));
    }

    byte[] encryptAESRequest(byte[] request) throws Exception{
        if(secretKey == null){
            return null;
        }
        return CryptTools.aesEncrypt(secretKey, Base64.getEncoder().encode(request));
    }

}
