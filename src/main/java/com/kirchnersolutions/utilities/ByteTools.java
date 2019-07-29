/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.utilities;
/**
 *2019 Kirchner Solutions
 * @author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.exceptions.DevelopmentException;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * ByteTools v1.0.05c
 *
 * @author Robert Kirchner Jr. 2018 Kirchner Solutions
 */
public class ByteTools {

    public byte[] remainderBytes = null;
    public int[] modInts = null;

    public byte[] fileToByteArray(File file) {
        List lBytes = Collections.synchronizedList(new ArrayList());
        try {
            InputStream in = new FileInputStream(file);
            int rByte, count = 0;
            byte[] eBytes;
            boolean found = false;
            while ((rByte = in.read()) != -1) {
                byte nByte = (byte) rByte;
                lBytes.add(nByte);
            }
            eBytes = new byte[lBytes.size()];
            Iterator itr = lBytes.listIterator();
            while (itr.hasNext()) {
                eBytes[count] = (byte) itr.next();
                count++;
            }
            return eBytes;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean byteArrayToFile(File file, byte[] bytes) {
        try {
            OutputStream out = new FileOutputStream(file);
            for (byte b : bytes) {
                out.write(b);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String bytesToString(byte[] bytes) {
        String string = new String(bytes, Charset.defaultCharset());
        return string;
    }

    public static boolean compareStringBuilder(StringBuilder s1, StringBuilder s2){
        if(s1.length() == s2.length()){
            for(int i = 0; i < s1.length(); i++){
                if(!(s1.charAt(i) == s2.charAt(i))){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    public byte[] attachModBytes(List modBytes, byte[] bytes) {
        byte[] newBytes = new byte[(((modBytes.size() * 5) + 4) + bytes.length)];
        int count = 0;
        if (modBytes.isEmpty()) {
            byte[] mods = this.intToByteArray(0);
            newBytes[count] = mods[0];
            count++;
            newBytes[count] = mods[1];
            count++;
            newBytes[count] = mods[2];
            count++;
            newBytes[count] = mods[3];
            count++;
            for (byte b : bytes) {
                newBytes[count] = b;
                count++;
            }
        } else {
            byte[] mods = this.intToByteArray(modBytes.size());
            newBytes[count] = mods[0];
            count++;
            newBytes[count] = mods[1];
            count++;
            newBytes[count] = mods[2];
            count++;
            newBytes[count] = mods[3];
            count++;
            Iterator itr = modBytes.listIterator();
            while (itr.hasNext()) {
                byte[] mod = (byte[]) itr.next();
                for (byte b : mod) {
                    newBytes[count] = b;
                    count++;
                }
            }
            for (byte b : bytes) {
                newBytes[count] = b;
                count++;
            }
        }
        return newBytes;
    }

    public byte[] removeModBytes(byte[] bytes) {
        List mInts = Collections.synchronizedList(new ArrayList());
        boolean finMods = false;
        byte[] newBytes = null;
        int count = 0, nCount = 0, mcount = 0;
        byte[] mods = new byte[4];
        mods[0] = bytes[count];
        count++;
        mods[1] = bytes[count];
        count++;
        mods[2] = bytes[count];
        count++;
        mods[3] = bytes[count];
        count++;
        int ms = this.byteArrayToInt(mods);
        System.out.println(bytes.length + "");
        while (count < bytes.length) {
            if (mcount < ms && finMods == false) {
                byte[] indInt = new byte[4];
                int conInt = -1;
                indInt[0] = bytes[count];
                count++;
                indInt[1] = bytes[count];
                count++;
                indInt[2] = bytes[count];
                count++;
                indInt[3] = bytes[count];
                count++;
                conInt = bytes[count];
                count++;
                int[] ints = new int[2];
                ints[0] = this.byteArrayToInt(indInt);
                if (conInt == 0) {
                    ints[1] = -1200000000;
                } else {
                    ints[1] = 1200000000;
                }
                mInts.add(ints);
                mcount++;
            } else if (mcount >= ms && finMods == false) {
                newBytes = new byte[bytes.length - (count)];
                finMods = true;
                System.out.println("Finished processing mods...");
            } else {
                newBytes[nCount] = bytes[count];
                nCount++;
                count++;
            }
        }
        System.out.println(count + "");
        this.modInts = new int[mInts.size() * 2];
        count = 0;
        Iterator itr = mInts.listIterator();
        while (itr.hasNext()) {
            int[] newInts = (int[]) itr.next();
            this.modInts[count] = newInts[0];
            count++;
            this.modInts[count] = newInts[1];
            count++;
        }
        return newBytes;
    }

    public int[] getModInts() {
        return this.modInts;
    }

    public int byteArrayToInt(byte[] eValue) {
        //System.out.println(Arrays.toString(eValue));
        int value = (eValue[3] << (Byte.SIZE * 3));
        value |= (eValue[2] & 0xFF) << (Byte.SIZE * 2);
        value |= (eValue[1] & 0xFF) << (Byte.SIZE * 1);
        value |= (eValue[0] & 0xFF);
        return value;
    }

    public byte[] intToByteArray(int value) {
        byte[] eValue = new byte[Integer.SIZE / Byte.SIZE];
        eValue[3] = (byte) (value >> Byte.SIZE * 3);
        eValue[2] = (byte) (value >> Byte.SIZE * 2);
        eValue[1] = (byte) (value >> Byte.SIZE);
        eValue[0] = (byte) value;
        return eValue;
    }

    public byte[] getRemainders() {
        return this.remainderBytes;
    }

    public List byteArrayToList(byte[] bytes) {
        List intBytes = Collections.synchronizedList(new ArrayList());
        byte[] intB = new byte[4];
        int count = 0, wholeCount = 0, wholeInt = 0, remainder = 0, rCount = 0, tCount = 1;
        if (bytes.length % 4 == 0) {
            this.remainderBytes = null;
            for (byte b : bytes) {
                intB[count] = b;
                if (count == 3) {
                    byte[] listAdd = new byte[4];
                    int c = 0;
                    for (byte by : intB) {
                        listAdd[c] = by;
                        c++;
                    }
                    intBytes.add(listAdd);
                    count = 0;
                } else {
                    count++;
                }
            }
        } else {
            wholeInt = bytes.length / 4;
            remainder = bytes.length - (4 * wholeInt);
            this.remainderBytes = null;
            this.remainderBytes = new byte[remainder];
            for (byte b : bytes) {
                if (wholeCount < wholeInt) {
                    if (count == 3) {
                        intB[count] = b;
                        byte[] listAdd = new byte[4];
                        int c = 0;
                        for (byte by : intB) {
                            listAdd[c] = by;
                            c++;
                        }
                        intBytes.add(listAdd);
                        count = 0;
                        wholeCount++;
                    } else {
                        intB[count] = b;
                        count++;
                    }
                } else {
                    this.remainderBytes[rCount] = b;
                    rCount++;
                }
                tCount++;
            }
        }
        return intBytes;
    }

    public List byteArrayToPaddedList(byte[] bytes) {
        List intBytes = Collections.synchronizedList(new ArrayList());
        byte[] intB = new byte[4];
        for (byte b : bytes) {
            intB[3] = b;
            intB[2] = 20;
            intB[1] = 20;
            intB[0] = 20;
            intBytes.add(intB);
        }
        return intBytes;
    }

    public int[] byteListToIntArray(List byteList) {
        int[] ints = new int[byteList.size()];
        int count = 0;
        //Iterator itr = byteList.listIterator();
        while (count < ints.length) {
            ints[count] = this.byteArrayToInt((byte[]) byteList.get(count));
            //System.out.println(ints[count]);
            count++;
        }
        //System.out.println(ints[0]);
        return ints;
    }

    public byte[] intArrayToByteArray(int[] ints, byte[] remainders) {
        List intBytes = Collections.synchronizedList(new ArrayList());
        byte[] bytes;
        if (remainders != null) {
            bytes = new byte[(ints.length * 4) + remainders.length];
        } else {
            bytes = new byte[(ints.length * 4)];
        }
        int count = 0;
        byte[] temp = new byte[4];
        for (int i : ints) {
            temp = this.intToByteArray(i);
            for (byte b : temp) {
                bytes[count] = b;
                count++;
            }
        }
        if (remainders != null) {
            for (byte b : remainders) {
                bytes[count] = b;
                count++;
            }
        }
        return bytes;
    }

    public byte[] removePadding(byte[] bytes) {
        byte[] unPad = new byte[bytes.length / 4];
        int count = 0, tcount = 0;
        for (byte b : bytes) {
            if ((count + 1) % 4 == 0) {
                unPad[tcount] = b;
                tcount++;
                count++;
            } else {
                count++;
            }
        }
        return unPad;
    }

    public int keyGen(String key) {
        byte[] oBytes = key.getBytes(Charset.defaultCharset());
        int genKey = 0;
        for (byte b : oBytes) {
            genKey += ((int) b) * 100;
        }
        //genKey = 0;
        return genKey;
    }

    /*
    public static byte[] readBytesFromFile(File file) throws IOException , Exception{
        while(!file.canRead()){

        }
        try{
            FileInputStream is = new FileInputStream(file);
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
            }
            byte[] bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
            return bytes;
        }catch (Exception e){
            System.out.println("Failed to get read lock on file " + file.getAbsolutePath());
            throw new DevelopmentException("Failed to get read lock on file " + file.getAbsolutePath() + " Class ByteTools Method readBytesFromFile");
        }

        /*FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
            FileLock fileLock;
            fileChannel.isOpen();
            try {

            } catch (Exception e) {
                System.out.println("File locked: retrying...");
                //System.out.println(e.getStackTrace().toString());
                e.printStackTrace();
                //return null;
                try {
                   Thread.sleep(500);
                    fileLock = fileChannel.tryLock();
                }catch (Exception ex){
                    ExceptionLogger.log(ex.toString() + BuildProps.getInstance().getNL() +
                            ex.getStackTrace().toString());
                }
            }
    }


     */


    public static byte[] readBytesFromFile(File file) throws IOException , Exception{
        BufferedInputStream bos = null;
        FileInputStream fos = null;
        byte[] bytes = null;
        while(!file.canRead()){

        }
        try {
            fos = new FileInputStream(file);
            FileChannel fileChannel = fos.getChannel();
            FileLock fileLock;
            try {
                while(!file.canRead()){

                }
                fileLock = fileChannel.tryLock();
            } catch (Exception e) {

            }
            bos = new BufferedInputStream(fos);
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
            }
            bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = bos.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            fos.close();
            bos.close();
        }
        return bytes;
    }


    public static void writeBytesToFile(File nFile, byte[] bytes) throws IOException, Exception {
        BufferedOutputStream bos = null;
        while(!nFile.canWrite()){

        }
        try {
            FileOutputStream fos = new FileOutputStream(nFile);
            FileChannel fileChannel = fos.getChannel();
            FileLock fileLock;
            try {
                while(!nFile.canWrite()){

                }
                fileLock = fileChannel.tryLock();
            } catch (Exception e) {
                System.out.println("File locked: retrying...");
                //return null;
                try {
                    Thread.sleep(500);
                    while(!nFile.canWrite()){

                    }
                    fileLock = fileChannel.tryLock();
                }catch (Exception ex){
                    System.out.println("Failed to get write lock on file " + nFile.getAbsolutePath());
                    throw new DevelopmentException("Failed to get write lock on file " + nFile.getAbsolutePath() + " Class ByteTools Method writeBytesToFile");
                }
            }
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (Exception e) {

                }
            }
        }
    }

    public byte[] compressByteArray(byte[] oBytes) {
        byte[] tBytes = new byte[oBytes.length];
        int i = 0, count = 0, tCount = 1;
        byte last = -1;
        while (i < oBytes.length) {
            if (i != 0) {
                if (oBytes[i] == last) {
                    tCount = 2;
                    byte b = oBytes[i];
                    i++;
                    byte c = oBytes[i];
                    if (c == b) {
                        while (c == b) {
                            tCount++;
                            i++;
                            c = oBytes[i];
                        }
                        if(tCount > 3){
                            tBytes[count] = -1;
                            count++;
                            tCount-= 1;
                            tBytes[count] = (byte)tCount;
                            count++;
                            tBytes[count] = c;
                            tCount = 0;
                            count++;
                            i++;
                        }else{
                           tBytes[count] = b;
                           count++;
                           tBytes[count] = b;
                           count++;
                           tBytes[count] = c;
                           count++;
                           i++;
                        }
                    } else {
                        tBytes[count] = b;
                        count++;
                        tBytes[count] = c;
                        count++;
                        i++;
                    }

                }else{
                    tBytes[count] = oBytes[i];
                    last = tBytes[count];
                    count++;
                    i++;
                }
            } else {
                tBytes[i] = oBytes[i];
                last = tBytes[i];
                count++;
                i++;
            }
        }
        byte[] cBytes = new byte[count];
        tCount = 0;
        while(tCount < count){
            cBytes[tCount] = tBytes[tCount];
            tCount++;
        }
        return cBytes;
    }
    
    public byte[] decompressByteArray(byte[] cBytes){
        List tBytes = Collections.synchronizedList(new ArrayList());
        int count = 0;
        boolean dup = false;
        byte t = -1;
        byte[] ints = new byte[4];
        ints[0] = cBytes[0];
        ints[1] = cBytes[1];
        ints[2] = cBytes[2];
        ints[3] = cBytes[3];
        System.out.println(this.byteArrayToInt(ints) + " MODS");
        for(int i = 0; i < cBytes.length; i++){
            byte next = cBytes[i];
            if(i == 0){
                System.out.println(next);
            }
            //tBytes.add(next);
            if(next == -1){
                i++;
                int qty = (int)cBytes[i];
                for(int k = 0; k < qty; k++){
                    byte t2 = t;
                    tBytes.add(t2);
                }
            }else{
                tBytes.add(next);
                t = next;
            }
        }
        byte[] dBytes = new byte[tBytes.size()];
        Iterator itr = tBytes.listIterator();
        int i = 0;
        while(itr.hasNext()){
            dBytes[i] = (byte)itr.next();
            i++;
        }
        return dBytes;
    }
}
