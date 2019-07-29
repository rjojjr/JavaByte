package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class DeleteRowsFromPage implements Callable<Map<BigInteger, Map<String, String>>> {

    private volatile TablePage tablePage;
    private List<BigInteger> indexes;

    public DeleteRowsFromPage(TablePage tablePage, List<BigInteger> indexes){
        this.tablePage = tablePage;
        this.indexes = indexes;
    }

    public Map<BigInteger, Map<String, String>> call() throws Exception{
        Thread.currentThread().setName(tablePage.getName().toString() + ":DeleteRowsFromPage:");
        Map<BigInteger, Map<String, String>> oldValues = new HashMap();
        for(BigInteger index : indexes){
            try{
                oldValues.put(index, tablePage.deleteRow(index));
            }catch (Exception e){
                throw e;
            }
        }
        return oldValues;
    }
}