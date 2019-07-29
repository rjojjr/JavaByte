package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.core.tables.TablePage;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateRows implements Callable<AtomicBoolean> {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private SysVars sysVars;

    private volatile List<TablePage> tables;
    private volatile Map<BigInteger, Map<String, String>> newRows;
    private volatile AtomicBoolean rollBackNeeded = new AtomicBoolean(false);

    public CreateRows(List<TablePage> tables, Map<BigInteger, Map<String, String>> newRows, ThreadPoolTaskExecutor threadPoolTaskExecutor, SysVars sysVars){
        this.tables = tables;
        this.newRows = newRows;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.sysVars = sysVars;
    }

    public AtomicBoolean call() throws Exception{
        Thread.currentThread().setName("Table " + tables.get(0).getName().split("-")[0] + ":CreateRows:");
       //tcdb System.out.println("Thread " + Thread.currentThread().getName() + " started...");
        Map<BigInteger, Map<String, String>>[] maps = new HashMap[tables.size()];
        for(BigInteger index : newRows.keySet()){

            int ind = tableIndexIsIn(index);
            //tcdbSystem.out.println(ind + ": Table Index");
            if(maps[ind] == null){
                Map<BigInteger, Map<String, String>> fields = new HashMap<>();
                fields.put(index, newRows.get(index));
                //System.out.println(index.toString());
                maps[ind] = fields;
            }else{
                maps[ind].put(index, newRows.get(index));
            }
        }
        List<Future<AtomicBoolean>> futures = new ArrayList<>();
        int count = 0;
        for(Map<BigInteger, Map<String, String>> map : maps){
            if(rollBackNeeded.get()){
                break;
            }
            if(map != null){
                //System.out.println(count + "");
                Future<AtomicBoolean> future = threadPoolTaskExecutor.submit(new CreateRowsInPage(tables.get(count), map, rollBackNeeded, sysVars));
                futures.add(future);
            }

            count++;
        }
        if(!rollBackNeeded.get()){
            for(Future<AtomicBoolean> future : futures){
                try{
                    if(!future.get().get()){
                        return rollBackNeeded;
                    }
                }catch (Exception e){
                    throw e;
                }
            }
        }
        return rollBackNeeded;
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
                t4 = n.subtract(new BigInteger("1"));
                return t4.intValue();
            }
            t4 = t4.subtract(new BigInteger("1"));
            return t4.intValue();
        }else{
            x = x.subtract(new BigInteger("1"));
            return x.intValue();
        }
    }

}