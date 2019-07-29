package com.kirchnersolutions.database.objects;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.Configuration.DevVars;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.utilities.SerialService.*;
import com.kirchnersolutions.database.exceptions.IllegalOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Component
@ApplicationScope
public class DatabaseObjectFactory {


    @Autowired
    private DevVars devVars;
    @Autowired
    private SysVars sysVars;

    private UserSerializer userSerializer = new UserSerializer();
    private TransactionSerializer transactionSerializer = new TransactionSerializer();
    private FieldSerializer fieldSerializer = new FieldSerializer(devVars, sysVars);
    private GeneralSerializer generalSerializer = new GeneralSerializer();
    private DatabaseResultSerializer databaseResultSerializer = new DatabaseResultSerializer();


    /**
     * Generates DatabaseObjectInterface implementation for given serial.
     *
     * @param serial
     * @return
     */
    public DatabaseObjectInterface databaseObjectFactory(byte[] serial) throws Exception{
        if(serial[0] == "3".getBytes("UTF-8")[0]){
            return userSerializer.deserialize(serial);
        }else if(serial[0] == "1".getBytes("UTF-8")[0]){
            return transactionSerializer.deserialize(serial);
        }else if(serial[0] == "2".getBytes("UTF-8")[0]){
            return fieldSerializer.deserialize(serial);
        }else if(serial[0] == "4".getBytes("UTF-8")[0]){

            return databaseResultSerializer.deserialize(serial);
        }else {

            throw new IllegalOperationException("Invalid ObjectFactory header");
        }
    }

    /**
     * Generates serial for given DatabaseObjectInterface implementation.
     * @param object
     * @return
     * @throws Exception
     */
    public byte[] databaseSerialFactory(DatabaseObjectInterface object) throws Exception{
        if(object.getHeader() == "1".getBytes("UTF-8")[0]){
            return transactionSerializer.serialize((Transaction)object);
        }
        if(object.getHeader() == "2".getBytes("UTF-8")[0]){
            return fieldSerializer.serialize((Field)object);
        }
        if(object.getHeader() == "3".getBytes("UTF-8")[0]){
            return userSerializer.serialize((User)object);
        }
        if(object.getHeader() == "4".getBytes("UTF-8")[0]){

            return databaseResultSerializer.serialize((DatabaseResults) object);
        }
        return null;
    }

    private byte[] attachHeader(DatabaseObjectInterface object) throws Exception{
        byte[] originalBytes = generalSerializer.serialize(object);
        byte[] newBytes = new byte[originalBytes.length + 1];
        newBytes[0] = object.getHeader();
        for(int i = 0; i < originalBytes.length; i++){
            newBytes[i + 1] = originalBytes[i];
        }
        return newBytes;
    }

    private byte[] removeHeader(byte[] bytes){
        byte[] newBytes = new byte[bytes.length - 1];
        for(int i = 0; i < newBytes.length; i++){
            newBytes[i] = bytes[i + 1];
        }
        return newBytes;
    }

}