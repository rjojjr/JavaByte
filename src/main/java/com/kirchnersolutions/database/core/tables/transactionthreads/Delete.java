package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.objects.Transaction;

import java.util.*;
import java.util.concurrent.Callable;

public class Delete implements Callable<Transaction> {

    private TransactionThreadService transactionThreadService;

    private volatile Transaction transaction;

    public Delete(Transaction transaction, TransactionThreadService transactionThreadService){
        this.transaction = transaction;
        this.transactionThreadService = transactionThreadService;
    }

    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":Delete:");
        if (transaction.getOperation().contains(" ALSO ")) {
            List<String> parts1 =  new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ALSO ")));
            List<String> parts =  new ArrayList<>(Arrays.asList(parts1.get(0).split(" ")));
            String table = parts.get(2);
            List<String> args =  new ArrayList<>(Arrays.asList(parts.get(4).split(";")));
            transaction.setWhere(getWhere(args));
            parts =  new ArrayList<>(Arrays.asList(parts1.get(1).split(" ")));
            String table2 = parts.get(0);
            args =  new ArrayList<>(Arrays.asList(parts.get(3).split(";")));
            transaction.setWhere2(getWhere(args));
            transaction.setOperation(new String("DELETE ADVANCED FROM " + table + ":" + table2));
            try {
                return transactionThreadService.deleteAdvanced(transaction);
            }catch (Exception e){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        } else {
            List<String> parts =  new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
            String table = parts.get(2);
            List<String> args =  new ArrayList<>(Arrays.asList(parts.get(4).split(";")));
            transaction.setOperation(new String("DELETE ADVANCED FROM " + table));
            transaction.setWhere(getWhere(args));
            try {
                return transactionThreadService.deleteAdvanced(transaction);
            }catch (Exception e){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }
    }

        private Map<String, String> getWhere(List<String> args){
            Map<String, String> where = new HashMap<>();
            for(String arg : args){
                List<String> sides =  new ArrayList<>(Arrays.asList(arg.split("=")));
                where.put(sides.get(0), sides.get(1));
            }
            return where;
        }
}