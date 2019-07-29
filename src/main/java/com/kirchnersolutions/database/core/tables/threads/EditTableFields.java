package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

class EditTableFields implements Callable<Map<BigInteger, Map<String, String>>> {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private List<TablePage> tables;
    private Map<BigInteger, Map<String, String>> changes;
    private volatile AtomicBoolean needRollback = new AtomicBoolean(false);

    public EditTableFields(List<TablePage> tables, Map<BigInteger, Map<String, String>> changes, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.tables = tables;
        this.changes = changes;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public Map<BigInteger, Map<String, String>> call() throws Exception{
        Thread.currentThread().setName("EditTableFields:");
        Future<Map<BigInteger, Map<String, String>>>[] futures = new Future[tables.size()];
        List<Map<BigInteger, Map<String, String>>> oldValues = new ArrayList<>();
        Map<BigInteger, Map<String, String>>[] pageMaps = new Map[tables.size()];
        for(BigInteger index : changes.keySet()){
            if(needRollback.get()){
                break;
            }
            int tableIndex = tableIndexIsIn(index);
            if(pageMaps[tableIndex] == null){
                Map<BigInteger, Map<String, String>> temp = new HashMap();
                temp.put(index, changes.get(index));
                pageMaps[tableIndex] = temp;
            }else{
                pageMaps[tableIndex].put(index, changes.get(index));
            }
        }
        List<String> tablesReq = new ArrayList<>();
        int count = 0;
        for (TablePage tablePage : tables) {
            if(pageMaps[count] != null){
                futures[count] = threadPoolTaskExecutor.submit(new EditFields(tablePage, changes, needRollback));
                tablesReq.add(count + "");
            }
            count++;
        }
        for (String ind : tablesReq) {
            try {
                Map<BigInteger, Map<String, String>> map = futures[Integer.parseInt(ind)].get();
                oldValues.add(map);
            } catch (Exception e) {
                try{
                    for(TablePage tablePage : tables){
                        tablePage.rollback(processMaps(oldValues));
                    }
                    throw e;
                }catch (Exception ex){
                    throw ex;
                }
            }
        }
        Map<BigInteger, Map<String, String>> result = processMaps(oldValues);
        if(needRollback.get()){
            for(TablePage tablePage : tables){
                try{
                    tablePage.rollback(result);
                }catch (Exception e){
                    throw e;
                }
            }
            return null;
        }else {
            return result;
        }
    }

    private int tableIndexIsIn(BigInteger index){
        BigInteger n = new BigInteger(tables.size() + "");
        BigInteger x = new BigInteger(index.toString());
        if(x.compareTo(n) == 1){
            int div = x.intValue() / n.intValue();
            BigInteger t1 = new BigInteger(div + "");
            BigInteger t2 = t1.multiply(n);
            BigInteger t4 = x.subtract(t2);
            if(t4.equals(new BigInteger("0"))){
                return n.intValue();
            }
            t4 = t4.subtract(new BigInteger("1"));
            return t4.intValue();
        }else{
            x = x.subtract(new BigInteger("1"));
            return x.intValue();
        }
    }

    private Map<BigInteger, Map<String, String>> processMaps(List<Map<BigInteger, Map<String, String>>> oldValues) {
        Map<BigInteger, Map<String, String>> result = new HashMap<>();
        for (Map<BigInteger, Map<String, String>> map : oldValues) {
            if (map == null) {
                needRollback.set(true);
            } else {
                List<BigInteger> keys = new ArrayList(map.keySet());
                for (BigInteger key : keys) {
                    result.put(key, map.get(key));
                }
            }
        }
        return result;
    }
}