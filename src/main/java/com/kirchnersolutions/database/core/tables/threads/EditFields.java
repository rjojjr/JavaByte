package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

class EditFields implements Callable<Map<BigInteger, Map<String, String>>> {

    private volatile TablePage tablePage;
    private Map<BigInteger, Map<String, String>> changes;
    private volatile AtomicBoolean needRollback;

    public EditFields(TablePage tablePage, Map<BigInteger, Map<String, String>> changes, AtomicBoolean needRollback){
        this.tablePage = tablePage;
        this.changes = changes;
        this.needRollback = needRollback;
    }

    public Map<BigInteger, Map<String, String>> call(){
        Thread.currentThread().setName(tablePage.getName() + ":EditFields:");
        try{
            return tablePage.editFields(changes, needRollback);
        }catch (Exception e){
            needRollback.set(true);
            return null;
        }
    }

}