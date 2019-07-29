package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;

public class DeleteTable implements Callable<Map<BigInteger, Map<String, String>>> {


    private volatile TablePage tablePage;

    public DeleteTable(TablePage tablePage){
        this.tablePage = tablePage;
    }

    public Map<BigInteger, Map<String, String>> call() throws Exception {
        Thread.currentThread().setName("Table " + tablePage.getName() + ":DeleteTable:");
        try{
            return tablePage.deleteTablePage();
        }catch (Exception e){
            throw e;
        }
    }
}