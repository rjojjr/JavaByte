package com.kirchnersolutions.database.Servers.HTTP.beans;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class StompTableInfo {

    private String tablename = "", tablepagecount = "", rowcount = "", currentindex = "";
    private String[] definition = null, indexedfields = null;

    public void setCurrentindex(String currentindex){
        int temp = Integer.parseInt(currentindex);
        temp+= 1;
        this.currentindex = "" + temp;
    }

    @Override
    public String toString() {
        if(indexedfields == null){
            return "Tablename:" + tablename + ",Definition:" + ArraytoString(definition) +
                    ",Indexed Fields:No indexed Fields" + ",Table Page Count:" + tablepagecount
                    + ",Row Count:" + rowcount + ",Current Index:" + currentindex;
        }else{
            return "Tablename:" + tablename + ",Definition:" + ArraytoString(definition) +
                    ",Indexed Fields:" + ArraytoString(indexedfields) + ",Table Page Count:" + tablepagecount
                    + ",Row Count:" + rowcount + ",Current Index:" + currentindex;
        }
    }

    private static String ArraytoString(String[] array){
        String result = "";
        boolean first = true;
        for(String string : array){
            if(first){
                result = string;
                first = false;
            }else{
                result+= " - " + string;
            }
        }
        return result;
    }
    /*
    @Override
    public String toString() {
        return "StompTableInfo [tablename=" + tablename + ", definition=" + Arrays.toString(definition) +
                ", indexedfields=" + Arrays.toString(indexedfields) + ", tablepagecount=" + tablepagecount
                + ", rowcounw=" + rowcount + ", currentindex=" + currentindex + "]";
    }

     */

}
