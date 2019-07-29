package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class SelectAdvanced implements Callable<Transaction> {

    private TableManagerService tableManagerService;

    private volatile Transaction transaction;
    private List<String> tableNames;

    public SelectAdvanced(Transaction transaction, List<String> tableNames, TableManagerService tableManagerService, DevVars devVars){
        this.transaction = transaction;
        this.tableNames = tableNames;
        this.tableManagerService = tableManagerService;
    }


    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":SelectAdvanced:");
        String expression = transaction.getOperation();
        if(tableNames.size() == 1) {
            String tableName = expression.split(":")[0];
            if (transaction.getHowMany().intValue() == -1) {
                try {
                    transaction.setResults(tableManagerService.searchTableAll(tableNames.get(0), transaction.getWhere()));
                    transaction.setSuccessfull(true);
                    transaction.setFinishTime(System.currentTimeMillis());
                    return transaction;
                } catch (Exception e) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String(e.getMessage()));
                    throw e;
                }
            } else {
                try {
                    transaction.setResults(tableManagerService.searchTable(tableNames.get(0), transaction.getWhere(), transaction.getHowMany().intValue()));
                    return transaction;
                } catch (Exception e) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String(e.getMessage()));
                    throw e;
                }
            }
        }else {
            try {
                List<Map<String, String>> rows = tableManagerService.searchTableAll(tableNames.get(0), transaction.getWhere());
                Map<String, String> where = transaction.getWhere2();
                if(where.keySet().size() > 1){
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("Invalid join conditions"));
                    return transaction;
                }
                String table1Field = null, table2Field = null;
                for(String key : where.keySet()){
                    table1Field = key;
                    table2Field = where.get(key);
                }
                Map<String, String> newRequest = new HashMap<>();
                Map<String, String> newRow = new HashMap<>();
                List<Map<String, String>> newResult = new ArrayList<>();
                List<Map<String, String>> result = new ArrayList<>();
                for(Map<String, String> row : rows){
                    for(String key : row.keySet()){
                        for(String wanted : transaction.getSelect()){
                            if(key.equals(wanted)){
                                newRow.put(key, row.get(key));
                                break;
                            }
                        }
                    }
                    newRequest.put(table2Field, row.get(table1Field));
                    try{
                        newResult = tableManagerService.searchTable(tableNames.get(1), newRequest, 1);
                    }catch (Exception e){
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String(e.getMessage()));
                        throw e;
                    }
                    if(newResult == null){
                        for(String wanted : transaction.getSelect()){
                            row.put(wanted, new String("null"));
                        }
                    }else{
                        for(String key : newResult.get(0).keySet()){
                            for(String wanted : transaction.getSelect()){
                                if(key.equals(wanted)){
                                    newRow.put(key, newResult.get(0).get(key));
                                    break;
                                }
                            }
                        }
                    }
                    result.add(newRow);
                }
                transaction.setResults(result);
                transaction.setSuccessfull(true);
                transaction.setFinishTime(System.currentTimeMillis());
                return transaction;
            } catch (Exception e) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }
    }
}