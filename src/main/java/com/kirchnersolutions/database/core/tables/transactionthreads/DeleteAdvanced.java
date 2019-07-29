package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

public class DeleteAdvanced implements Callable<Transaction> {

    private TableManagerService tableManagerService;

    private volatile Transaction transaction;

    public DeleteAdvanced(Transaction transaction, TableManagerService tableManagerService){
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
    }

    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":DeleteAdvanced:");
        List<String> parts =  new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        List<String> tables =  new ArrayList<>(Arrays.asList(parts.get(3).split(":")));
        if(tables.size() > 1 && !tables.get(1).equals("")){
            List<Map<BigInteger, Map<String, String>>> oldValues = new ArrayList<>();
            Map<List<BigInteger>, List<BigInteger>> indexSet = getBothIndexes(tables);
            List<BigInteger> indexes1 = (List<BigInteger>)indexSet.keySet().toArray()[0];
            List<BigInteger> indexes2 = (List<BigInteger>)indexSet.get(indexes1);
            try {
                oldValues.add(tableManagerService.deleteRows(indexes1, tables.get(0)));
                oldValues.add(tableManagerService.deleteRows(indexes2, tables.get(1)));
            }catch (Exception e){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
            transaction.setOldValues(oldValues);
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
            return transaction;
        }else{
            List<BigInteger> indexes = getIndexes(tables.get(0));
            List<Map<BigInteger, Map<String, String>>> oldValues = new ArrayList<>();
            try {
                oldValues.add(tableManagerService.deleteRows(indexes, tables.get(0)));
            }catch (Exception e){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
            transaction.setOldValues(oldValues);
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
            return transaction;
        }
    }

    private Map<List<BigInteger>, List<BigInteger>> getBothIndexes(List<String> tables) throws Exception{
        List<Map<String, String>> rows = new ArrayList<>();
        try{
            rows = tableManagerService.searchTableAll(tables.get(0), transaction.getWhere());
        }catch (Exception e){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
        List<BigInteger> results = new ArrayList<>();
        List<BigInteger> results2 = new ArrayList<>();
        for(Map<String, String> row : rows){
            BigInteger index = new BigInteger(row.get(new String("index")).toString());
            results.add(index);
            Map<String, String> where2 = new HashMap<>();
            where2.put(transaction.getWhere2().get((String)transaction.getWhere2().keySet().toArray()[0]),row.get(transaction.getWhere2().keySet().toArray()[0]));
            try{
                List<Map<String, String>> nrow = tableManagerService.searchTableAll(tables.get(2), where2);
                results2.add(new BigInteger(nrow.get(0).get(new String("index")).toString()));
            }catch (Exception e){
                //Rollback
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }
        Map<List<BigInteger>, List<BigInteger>> result = new HashMap<>();
        result.put(results, results2);
        return result;
    }

    private List<BigInteger> getIndexes(String table) throws Exception{
        List<Map<String, String>> rows = new ArrayList<>();
        try{
            rows = tableManagerService.searchTableAll(table, transaction.getWhere());
        }catch (Exception e){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
        List<BigInteger> results = new ArrayList<>();
        for(Map<String, String> row : rows){
            BigInteger index = new BigInteger(row.get(new String("index")).toString());
            results.add(index);
        }
        return results;
    }
}