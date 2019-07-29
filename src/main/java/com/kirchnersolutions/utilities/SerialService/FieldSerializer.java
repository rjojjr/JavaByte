package com.kirchnersolutions.utilities.SerialService;
/**
 * 2019 Kirchner Solutions
 *
 * @Author Robert Kirchner Jr.
 * <p>
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.SerialException;
import com.kirchnersolutions.database.objects.Field;
import com.kirchnersolutions.utilities.ByteTools;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Base64;


public class FieldSerializer implements Serializer<Field> {

    private DevVars devVars;
    private SysVars sysVars;

    public FieldSerializer(DevVars devVars, SysVars sysVars) {
        this.sysVars = sysVars;
        this.devVars = devVars;
    }

    /**
     *Serialize Field object into byte[].
     * @param object
     * @Overide
     * @return
     * @throws Exception
     */
    @Override
    public byte[] serialize(Field object) throws Exception {
        if (object == null) {

        }
        byte[] nameBytes, valueBytes, typeBytes, finalBytes;
        nameBytes = object.getName().getBytes("UTF-8");
        valueBytes = object.getValue().getBytes();
        typeBytes = object.getType().getBytes("UTF-8");
        try {

            byte[] serial = new byte[nameBytes.length + typeBytes.length + valueBytes.length + 2];
            int count = 0;
            for (int i = 0; count < nameBytes.length; count++) {
                serial[count] = nameBytes[count];
            }
            byte breakTag = "~".getBytes("UTF-8")[0];
            serial[count] = breakTag;
            count++;
            for (int i = 0; i < valueBytes.length; count++) {
                serial[count] = valueBytes[i];
                i++;
            }
            serial[count] = breakTag;
            count++;
            for (int i = 0; i < typeBytes.length; count++) {
                serial[count] = typeBytes[i];
                i++;
            }
            //System.out.println("here");
            finalBytes = new byte[serial.length + 1];
            finalBytes[0] = object.getHeader();
            for (int i = 0; i < serial.length; i++) {
                finalBytes[i + 1] = serial[i];
            }
            finalBytes = Base64.getEncoder().encode(finalBytes);
            return finalBytes;
        } catch (ArrayIndexOutOfBoundsException ex) {

            //Rollback imp

            System.out.println("Serial index count is off");
            System.out.println(ex);
            throw new DevelopmentException("Serial index count is off: Class Field Method serialize()");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //;
        return null;
    }

    /**
     *Deserialize Field object from byte[].
     * @param serial
     * @Overide
     * @return
     * @throws Exception
     */
    @Override
    public Field deserialize(byte[] serial) throws Exception {
        //System.out.println(serial.length + "serial length");
        serial = Base64.getDecoder().decode(serial);
        if (serial[0] != "2".getBytes("UTF-8")[0]) {

            return null;
        }
        byte[] originalBytes = new byte[serial.length - 1];
        for (int i = 0; i < originalBytes.length; i++) {
            originalBytes[i] = serial[i + 1];
        }
        serial = originalBytes;
        int temp = 0;
        if(new String(serial, "UTF-8").split("~").length > 3){
            temp = new String(serial).split("~").length;
            temp = temp -3;
        }
        int countt = 0;
        Field field = new Field();
        byte[] nameBytes, valueBytes, typeBytes;
        char breakTag = '\n';
        byte bTag = "~".getBytes("UTF-8")[0];
        try {
            boolean first = true;
            int count = -1, nameLength = 0, valueLength = 0, typeLength = 1;
            for (int i = 0; i < serial.length; i++) {
                if (serial[i] == bTag) {
                    if (nameLength == 0) {
                        nameLength = i;

                        first = false;
                    } else if (valueLength == 0) {
                        if(temp > 0 && countt < temp){
                            countt++;
                        }else{
                            valueLength = count;
                            //System.out.println(count + " value");
                            break;
                        }
                    }
                }
                if (!first) {
                    count++;
                }
            }

            nameBytes = new byte[nameLength];
            valueBytes = new byte[valueLength];
            typeBytes = new byte[typeLength];
            count = 1;
            for (int i = 0; i < nameLength; i++) {
                nameBytes[i] = serial[i];
                count++;
            }
            int count1 = 0;
            for (int i = count; count1 < valueLength; i++) {
                valueBytes[count1] = serial[i];
                count++;
                count1++;
            }
            count1 = 0;
            for (int i = count; count1 < typeLength; i++) {
                typeBytes[count1] = serial[i];
                count++;
                count1++;
            }
            try {
                field.setName(new String(nameBytes, "UTF-8"));
            } catch (SerialException ex1) {

                if (true) {
                    ex1.printStackTrace();
                    System.out.println("Failed to deserialize field name");
                    throw new DevelopmentException("Failed to deserialize field name: Class Field Method deserialize()");
                }
                throw new SerialException("Failed to deserialize field name");
            }
            try {
                //System.out.println(new String(valueBytes));
                field.setValue(new String(valueBytes));
            } catch (SerialException ex1) {

                if (true) {
                    System.out.println("Failed to deserialize field value");
                    throw new DevelopmentException("Failed to deserialize field value: Class Field Method deserialize()");
                }
                throw new SerialException("Failed to deserialize field value");
            }
            field.setType(new String(typeBytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            if (true) {
                System.out.println("Field type encoding not supported: " + sysVars.getCharacterEncoding());
                System.out.println(e);
                throw new DevelopmentException("Field type encoding not supported: " + sysVars.getCharacterEncoding() + " Class Field Method deserialize()");
            } else {
                throw new SerialException("Field type encoding not supported: " + sysVars.getCharacterEncoding());
            }

        } catch (ArrayIndexOutOfBoundsException ex) {
            //Rollback imp
            if (true) {
                System.out.println("Serial index count is off");
                System.out.println(ex);
                throw new DevelopmentException("Serial index count is off: Class Field Method deserialize()");
            }
        }catch (Exception exc) {
            exc.printStackTrace();
        }

        return field;
    }

    public Field deserialize(File file) throws Exception {
        try {
            if (!file.exists()) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + " doesn't exist");
                    throw new DevelopmentException("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + " doesn't exist" + " Class Field Method deserialize(File file)");
                } else {
                    throw new SerialException("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + " doesn't exist");
                }
            }
            byte[] bytes = ByteTools.readBytesFromFile(file);
            Field field = deserialize(bytes);

            field.setFile(file);
            return field;
        } catch (Exception e) {
            if (false) {
                System.out.println("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + e.getMessage());
                System.out.println(e);
                throw new DevelopmentException("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + e.getMessage() + " Class Field Method deserialize(File file)");
            } else {
                e.printStackTrace();
                throw new SerialException("Field deserialization error: Field file " + file.getParent() + "/" + file.getName() + e.getMessage());

            }
        }
    }
}