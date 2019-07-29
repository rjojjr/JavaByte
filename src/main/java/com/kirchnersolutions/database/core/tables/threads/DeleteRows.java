package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

class DeleteRows implements Callable<Map<BigInteger, Map<String, String>>> {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private List<BigInteger> indexes;
    private volatile List<TablePage> tables;

    public DeleteRows(List<BigInteger> indexes, List<TablePage> tables, ThreadPoolTaskExecutor threadPoolTaskExecutor){
        this.indexes = indexes;
        this.tables = tables;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public Map<BigInteger, Map<String, String>> call() throws Exception{
        Thread.currentThread().setName("Table " + tables.get(0).getName().split("-")[0] + ":DeleteRows:");
        List<Map<BigInteger, Map<String, String>>> oldValuesArray = new ArrayList<>();
        List<BigInteger>[] tableQeue = new ArrayList[tables.size()];
        for(BigInteger index : indexes){
            int ind = tableIndexIsIn(index);
            if (tableQeue[ind] != null) {
                tableQeue[ind].add(index);
            }else{
                tableQeue[ind] = new ArrayList<>();
                tableQeue[ind].add(index);
            }
        }
        List<Future<Map<BigInteger, Map<String, String>>>> futures = new ArrayList<>();
        for(int i = 0; i < tableQeue.length; i++){
            if(tableQeue[i] != null){
                Future<Map<BigInteger, Map<String, String>>> future = threadPoolTaskExecutor.submit(new DeleteRowsFromPage(tables.get(i), tableQeue[i]));
                futures.add(future);
            }

        }
        for(Future<Map<BigInteger, Map<String, String>>> future : futures){
            oldValuesArray.add(future.get());
        }
        Map<BigInteger, Map<String, String>> oldValues = new HashMap<>();
        return processMaps(oldValuesArray);
    }

    private Map<BigInteger, Map<String, String>> processMaps(List<Map<BigInteger, Map<String, String>>> oldValues) {
        Map<BigInteger, Map<String, String>> result = new HashMap<>();
        for (Map<BigInteger, Map<String, String>> map : oldValues) {
                List<BigInteger> keys = new ArrayList(map.keySet());
                for (BigInteger key : keys) {
                    result.put(key, map.get(key));
                }
        }

        return result;
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
}