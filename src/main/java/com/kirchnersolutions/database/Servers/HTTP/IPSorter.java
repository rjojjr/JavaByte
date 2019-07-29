package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.utilities.CalenderConverter;

import java.io.File;
import java.util.Comparator;

class IPSorter implements Comparator<File> {

    public int compare(File m1, File m2){
        long m1i = getFileTime(m1);
        long m2i = getFileTime(m2);
        if(m1i < m2i){
            return 1;
        }
        if(m1i > m2i){
            return -1;
        }
        return 0;
    }

    private long getFileTime(File log){
        String[] temp = log.getName().split("~");
        String[] temp1 = temp[0].split("-");
        return CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3].split("_")[0]));
    }
}
