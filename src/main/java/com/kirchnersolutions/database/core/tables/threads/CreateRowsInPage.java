package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.core.tables.TablePage;

import java.io.File;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

class CreateRowsInPage implements Callable<AtomicBoolean> {

    private SysVars sysVars;

    private volatile TablePage tablePage;
    private Map<BigInteger, Map<String, String>> newRows;
    private volatile AtomicBoolean rollBackNeeded;


    public CreateRowsInPage(TablePage tablePage, Map<BigInteger, Map<String, String>> newRows, AtomicBoolean rollBackNeeded, SysVars sysVars){
        this.tablePage = tablePage;
        this.newRows = newRows;
        this.rollBackNeeded = rollBackNeeded;
        this.sysVars = sysVars;
    }

    public AtomicBoolean call() throws Exception{
        Thread.currentThread().setName("Table " + tablePage.getName().split("-")[0] + " Page index " + tablePage.getName().split("-")[1] + ":CreateRowsInPage:");
        //System.out.println("Thread " + Thread.currentThread().getName() + " started...");
        for(BigInteger index : newRows.keySet()){
            try{
                if(rollBackNeeded.get()){
                    break;
                }
                tablePage.createRow(index, newRows.get(index), rollBackNeeded);
            }catch (Exception e){
                System.out.println("Rollback Initiated");
                e.printStackTrace();
                rollBackNeeded.set(true);
                break;
            }
        }
        if(rollBackNeeded.get()){
            for(BigInteger index : newRows.keySet()){

                File dir = new File(tablePage.getRowDir(), sysVars.getFileSeperator() + index.toString());
                if(dir.exists()){
                    tablePage.removeRowsFromMaps(index);
                    File[] files = dir.listFiles();
                    for(File t : files){
                        t.delete();
                    }
                    dir.delete();
                }else {
                    break;
                }
            }
        }
        return rollBackNeeded;
    }

}