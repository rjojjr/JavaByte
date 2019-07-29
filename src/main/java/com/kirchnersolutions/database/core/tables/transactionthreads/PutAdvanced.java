package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

public class PutAdvanced implements Callable<Transaction> {

    private TableManagerService tableManagerService;

    private volatile Transaction transaction;

    public PutAdvanced(Transaction transaction, TableManagerService tableManagerService, DevVars devVars){
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
    }

    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":PutAdvanced:" );
        List<String> parts = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        List<String> tables = new ArrayList<>(Arrays.asList(parts.get(2).split(":")));
        if(tables.size() > 1 && !tables.get(1).equals(new String(""))){
            List<Map<String, String>> where2s = new ArrayList<>();
            Map<BigInteger, Map<String, String>> newRow = getNewRows(tables.get(0), transaction.getPut(), transaction.getWhere());
            for(BigInteger key : newRow.keySet()){
                Map<String, String> map = newRow.get(key);
                Map<String, String> nmap = new HashMap<>();
                nmap.put(transaction.getWhere2().get((String)transaction.getWhere2().keySet().toArray()[0]),map.get(transaction.getWhere2().keySet().toArray()[0]));
                where2s.add(nmap);
            }
            try{
                tableManagerService.editRows(newRow, tables.get(0));
            }catch (Exception e){
                e.printStackTrace();
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
            for(Map<String, String> map : where2s){
                newRow = getNewRows(tables.get(1), transaction.getPut2(), map);
                try{
                    tableManagerService.editRows(newRow, tables.get(0));
                }catch (Exception e){
                    //Rollback
                    e.printStackTrace();
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String(e.getMessage()));
                    throw e;
                }
            }
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
            return transaction;
        }else{
            String tableName = tables.get(0);

            Map<BigInteger, Map<String, String>> newRow = getNewRows(tableName, transaction.getPut(), transaction.getWhere());
            if(newRow == null){
               //System.out.println(tableName);
            }
            try{
                tableManagerService.editRows(newRow, tableName);
                transaction.setSuccessfull(true);
                transaction.setFinishTime(System.currentTimeMillis());
                return transaction;
            }catch (Exception e){
                e.printStackTrace();
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }
    }

    private Map<BigInteger, Map<String, String>> getNewRows(String tableName, Map<String, String> put, Map<String, String> where) throws Exception {
        List<Map<String, String>> targets = new ArrayList<>();
        try{
            targets = tableManagerService.searchTableAll(tableName, where);
        }catch (Exception e){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
        if(targets.isEmpty() || targets == null){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("No rows match criteria."));
            return null;
        }
        List<Map<BigInteger, Map<String, String>>> oldValues = new ArrayList<>();
        List<Map<BigInteger, Map<String, String>>> newValues = new ArrayList<>();
        Map<BigInteger, Map<String, String>> newRow = new HashMap<>();
        for(Map<String, String> row : targets) {

            Map<BigInteger, Map<String, String>> oldRow = new HashMap<>();
            Map<String, String> map = new HashMap<>();
            oldRow.put(new BigInteger(row.get(new String("index")).toString()), row);
            oldValues.add(oldRow);
            for (String key : put.keySet()) {
                row.put(key, put.get(key));
            }
            newRow.put(new BigInteger(row.get(new String("index")).toString()), row);
        }
        return newRow;
    }
}