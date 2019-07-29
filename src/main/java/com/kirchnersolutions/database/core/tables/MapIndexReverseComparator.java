package com.kirchnersolutions.database.core.tables;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;

public class MapIndexReverseComparator implements Comparator<Map<String, String>> {

    public int compare(Map<String, String> m1, Map<String, String> m2){
        BigInteger m1i = new BigInteger(m1.get("index"));
        BigInteger m2i = new BigInteger(m2.get("index"));
        int comp = m1i.compareTo(m2i);
        if(comp == 1){
            return -1;
        }
        if(comp == -1){
            return 1;
        }
        return 0;
    }
}
