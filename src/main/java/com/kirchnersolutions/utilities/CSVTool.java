/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.utilities;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 *
 * CSVTool Version: 1.0.04b
 *
 * @author Robert Kirchner Jr. 
 * 2018 Kirchner Solutions
 */
public class CSVTool {

    public static final String VERSION = "1.0.04b";
    private File temp, path;

    /**
     * Uses only default constructor
     */
    public CSVTool() {

    }

    /**
     *
     * @param db
     * @param index
     * @param value
     * @return
     */
    public String searchCSV(File db, int index, String value) {
        String entry = "failed";
        Scanner in;
        try {
            in = new Scanner(db);
            int found = 0;
            while (in.hasNextLine() && found == 0) {
                entry = in.nextLine();
                String[] data = entry.split(",");
                if (data[index].equals(value)) {
                    found = 1;
                } else {
                    entry = "failed";
                }
            }
        } catch (Exception e) {
        }
        return entry;
    }
    
    public static String readFromRandomAccessFile(String file, int position) {
        String record = null;
        try {
            RandomAccessFile fileStore = new RandomAccessFile(file, "rw");

            // moves file pointer to position specified
            fileStore.seek(position);

            // reading String from RandomAccessFile
            record = fileStore.readUTF();

            fileStore.close();

        } catch (IOException e) {}

        return record;
    }

    public static void writeToRandomAccessFile(String file, int position, String record) {
        try {
            RandomAccessFile fileStore = new RandomAccessFile(file, "rw");

            // moves file pointer to position specified
            fileStore.seek(position);

            // writing String to RandomAccessFile
            fileStore.writeUTF(record);

            fileStore.close();

        } catch (IOException e) {
        }
    }

    public static List consolidateListCache(List list1, List list2, List list3) {
        List list4 = list2;
        Iterator itr = list1.iterator();
        while (itr.hasNext()) {
            String item1 = (String)itr.next();
            Iterator itr2 = list2.iterator();
            int count = 0;
            while(itr2.hasNext()){
                String item2 = (String)itr2.next();
                if(item1.equals(item2)){
                    list4.set(count, null);
                }
                count++;
            }
        }
        Iterator itr3 = list4.iterator();
        while(itr3.hasNext()){
           String item3 = (String)itr.next();
           if(item3 != null){
               list3.add("n " + item3);
           }
        }
        return list1;
    }
    
    public static List removeStringFromList(List list, String item){
        List result = new ArrayList<String>();
        Iterator itr = list.iterator();
        while(itr.hasNext()){
            String temp = (String)itr.next();
            if(!temp.equals(item)){
                result.add(temp);
            }
        }
        return result;
    }
    
    public static List consolidateList(List list1, List list2) {
        List list3 = list2;
        Iterator itr = list1.iterator();
        while (itr.hasNext()) {
            String item1 = (String)itr.next();
            Iterator itr2 = list2.iterator();
            int count = 0;
            while(itr2.hasNext()){
                String item2 = (String)itr2.next();
                if(item1.equals(item2)){
                    list3.set(count, null);
                }
            }
        }
        Iterator itr3 = list3.iterator();
        while(itr3.hasNext()){
           String item3 = (String)itr.next();
           if(item3 != null){
               list1.add(item3);
           }
        }
        return list1;
    }
    
    public static List consolidateMatches(List l1, List l2){
        List result = new ArrayList<String>();
        Iterator itr1 = l1.iterator();
        while(itr1.hasNext()){
            String temp1 = (String)itr1.next();
            Iterator itr2 = l2.iterator();
            while(itr2.hasNext()){
                String temp2 = (String)itr2.next();
                if(temp1.equals(temp2)){
                    result.add(temp1);
                    break;
                }
            }
        }
        return result;
    }

    public boolean addEntry(File db, String entry) {
        boolean worked = false, first = true;
        try {
            Scanner in = new Scanner(db);
            FileWriter out = new FileWriter(db, true);
            String temp = in.nextLine();
            if (temp.equals("")) {
                out.write(entry);
            } else {
                out.write("\r\n" + entry);
            }
            out.close();
            worked = true;
        } catch (Exception e) {
            System.err.println("ERROR 1036: FAILED TO ADD ENTRY TO DATABASE");
        }
        return worked;
    }

    public boolean deleteEntry(File db, int index, String value) {
        boolean worked = false, first = true;
        String line;
        try {
            FileWriter outt = new FileWriter(temp);
            Scanner inp = new Scanner(db);
            while (inp.hasNextLine()) {
                line = inp.nextLine();
                String[] data = line.split(",");
                if (data[index].equals(value)) {

                } else {
                    if (first) {
                        outt.write(line);
                        first = false;
                    } else {
                        outt.write("\r\n" + line);
                    }
                }
            }
            first = true;
            outt.close();
            inp.close();
            outt = new FileWriter(db);
            inp = new Scanner(temp);
            while (inp.hasNextLine()) {
                line = inp.nextLine();
                if (first) {
                    outt.write(line);
                    first = false;
                } else {
                    outt.write("\r\n" + line);
                }
            }
            inp.close();
            outt.close();
            worked = true;
        } catch (Exception e) {
            worked = false;
        }
        return worked;
    }

    public boolean editEntry(File db, int index, String value, String entry) {
        boolean worked = false, first = true;
        String line;
        try {
            FileWriter outt = new FileWriter(temp);
            Scanner inp = new Scanner(db);
            while (inp.hasNextLine()) {
                line = inp.nextLine();
                String[] data = line.split(",");
                if (data[index].equals(value)) {
                    if (first) {
                        outt.write(entry);
                        first = false;
                    } else {
                        outt.write("\r\n" + entry);
                    }
                } else {
                    if (first) {
                        outt.write(line);
                        first = false;
                    } else {
                        outt.write("\r\n" + line);
                    }
                }
            }
            outt.close();
            inp.close();
            outt = new FileWriter(db);
            inp = new Scanner(temp);
            while (inp.hasNextLine()) {
                line = inp.nextLine();
                if (first) {
                    outt.write(line);
                    first = false;
                } else {
                    outt.write("\r\n" + line);
                }
            }
            inp.close();
            outt.close();
            worked = true;
        } catch (Exception e) {
            worked = false;
        }
        return worked;
    }

    public static String arrayToString(String[] values) {
        boolean first = true;
        String expression = "";
        for (String value : values) {
            if (first) {
                expression = value;
                first = false;
            } else {
                expression = expression + "," + value;
            }
        }
        return expression;
    }

    public String[] getLineArray(File db) {
        String lines = "";
        boolean first = true;
        try {
            Scanner in = new Scanner(db);
            while (in.hasNextLine()) {
                if (first) {
                    lines = in.nextLine();
                    first = false;
                } else {
                    lines = lines + ":" + in.nextLine();
                }
            }
            return lines.split(":");
        } catch (Exception e) {
            System.err.println("ERROR 1064: FAILURE TO GET LINE ARRAY");
            return null;
        }
    }

    public boolean writeInvFile(File db, String[] names, String[] qty) {
        try {
            if (names.length == qty.length) {
                if (db.delete() && db.createNewFile()) {
                    String line = "";
                    for (int i = 0; i < names.length; i++) {
                        line = names[i] + "," + qty[i];
                        if (qty[i].equals("0")) {

                        } else {
                            if (!this.addEntry(db, line)) {
                                System.err.println("WARNING: FAILED TO WRITE OBJECT " + names[i] + " TO DISK");
                            }
                        }
                    }
                    return true;
                } else {
                    System.err.println("ERROR 1068: FAILURE TO REFRESH INVENTORY FILE");
                    return false;
                }
            } else {
                System.err.println("ERROR 1067: MISSMATCHED INVENTORY QUANTITY");
                return false;
            }
        } catch (Exception e) {
            System.err.println("ERROR 1066: FAILURE TO CREATE INVENTORY OBJECT FILE");
            return false;
        }
    }

    public static <T> T concatenateArray(T a, T b) {
        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            throw new IllegalArgumentException();
        }

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        } else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        } else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }
}
