package com.kirchnersolutions.utilities.SerialService;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.objects.User;

import java.util.HashMap;


public class UserSerializer implements Serializer<User> {

    /**
     *Serialize User object into byte[].
     * @param object
     * @return
     * @throws Exception
     */
    @Override
    public byte[] serialize(User object) throws Exception {
        byte[] hash = serializeObject(object.getDetails());
        byte[] headered = new byte[hash.length + 1];
        headered[0] = object.getHeader();
        for (int i = 0; i < hash.length; i++){
            headered[i + 1] = hash[i];
        }
        return headered;
    }

    /**
     * Deserialize User object from byte[].
     * @param bytes
     * @return
     * @throws Exception
     */
    @Override
    public User deserialize(byte[] bytes) throws Exception {
        if(bytes[0] != (byte)3){
            return null;
        }
        byte[] originalBytes = new byte[bytes.length - 1];
        for (int i = 0; i < originalBytes.length; i++){
            originalBytes[i] = bytes[i + 1];
        }
        return new User((HashMap<String, String>)deserializeObject(originalBytes));
    }
}