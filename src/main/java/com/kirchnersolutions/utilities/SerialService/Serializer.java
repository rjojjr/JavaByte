package com.kirchnersolutions.utilities.SerialService;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.exceptions.SerialException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Serials will have one byte int header.
 * 1 = transaction.
 * 2 = field.
 * 3 = user.
 * 4 = config.
 * -1 = general
 * @param <A>
 */
public interface Serializer<A> {

    /**
     * Serialize implements Serializable Object into byte[].
     * @param object
     * @return
     * @throws SerialException
     */
    default byte[] serializeObject(Object object) throws SerialException{
        if(object != null){
            byte[] bytes;
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                ByteBuffer buffer;
                out = new ObjectOutputStream(bos);
                out.writeObject(object);
                out.close();
                bos.close();
                bytes = bos.toByteArray();
            } catch (Exception e) {
                throw new SerialException("Failed to serialize object");
            }
            return bytes;
        }else{
            return null;
        }
    }

    /**
     * Deserialize implements Serializable Object from byte[].
     * @param bytes
     * @return
     * @throws SerialException
     */
    default Object deserializeObject(byte[] bytes) throws SerialException{
        Object o;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            o = (Object)in.readObject();
            in.close();
            bis.close();;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SerialException("Failed to deserialize object");
        }
        return o;
    }

    /**
     * Serialize A Object into byte[].
     * @param object
     * @return
     * @throws Exception
     */
    byte[] serialize(A object) throws Exception;

    /**
     * Deserialize A Object from byte[].
     * @param bytes
     * @return
     * @throws Exception
     */
    A deserialize(byte[] bytes) throws Exception;

}