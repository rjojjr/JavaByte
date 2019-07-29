package com.kirchnersolutions.database.core.tables;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;

public class IndexReverseComparator implements Comparator<String> {

        public int compare(String m1, String m2){
            BigInteger m1i = new BigInteger(m1);
            BigInteger m2i = new BigInteger(m2);
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
