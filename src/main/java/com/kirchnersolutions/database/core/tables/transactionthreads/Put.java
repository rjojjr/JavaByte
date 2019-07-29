package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.objects.Transaction;

import java.util.*;
import java.util.concurrent.Callable;

public class Put implements Callable<Transaction> {

    private TransactionThreadService transactionThreadService;

    private volatile Transaction transaction;

    public Put(Transaction transaction, TransactionThreadService transactionThreadService){
        this.transaction = transaction;
        this.transactionThreadService = transactionThreadService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":PutAdvanced:");
        if(transaction.getOperation().contains(" ALSO ")){
            List<String> parts = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ALSO ")));
            if(parts == null || parts.size() == 1 || parts.size() > 2){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid argument format."));
                return transaction;
            }
            if(put(parts.get(0), 1) == null){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid argument format."));
                return transaction;
            }
            if(put(parts.get(1), 2) == null){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid argument format."));
                return transaction;
            }
            try{
                return transactionThreadService.putAdvanced(transaction);
            }catch (Exception e){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }else{
            if(put(transaction.getOperation(), 1) == null){
                return transaction;
            }
            return transactionThreadService.putAdvanced(transaction);
        }
    }

    private Transaction put(String firstExpression, int which){
        List<String> parts =  new ArrayList<>(Arrays.asList(firstExpression.split(" WHERE ")));
        if(parts.size() == 1 || parts.get(1).equals(new String("")) || parts.size() > 2) {
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("Invalid argument format."));
            return null;
        }
        Map<String, String> put = new HashMap<>();
        String tableName = parts.get(0).split(" ")[0];
        List<String> putParts = new ArrayList<>();
        if(which == 1){
            String puts = parts.get(0).replace("PUT ", "");
            puts = puts.replace(tableName + " ", "");
            putParts =  new ArrayList<>(Arrays.asList(puts.split(";")));
        }else{
            String puts = parts.get(0).replace(tableName + " ", "");
            putParts =  new ArrayList<>(Arrays.asList(puts.split(";")));
        }
        for (String part : putParts){
            List<String> command =  new ArrayList<>(Arrays.asList(part.split("=")));
            if(command.size() == 1 || command.get(1).equals(new String("")) || command.size() > 2) {

                return null;
            }
            put.put(command.get(0), command.get(1));
        }
        if(which == 1){
            transaction.setPut(put);
        }else {
            transaction.setPut2(put);
        }
        Map<String, String> where = new HashMap<>();
        putParts =  new ArrayList<>(Arrays.asList(parts.get(1).split(";")));
        for (String part : putParts){
            List<String> command =  new ArrayList<>(Arrays.asList(part.split("=")));
            if(command.size() == 1 || command.get(1).equals(new String("")) || command.size() > 2) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid argument format."));
                return null;
            }
            where.put(command.get(0), command.get(1));
        }
        if(which == 1){
            transaction.setWhere(where);
            transaction.setOperation(new String("PUT ADVANCED " + tableName));
        }else{
            transaction.setWhere2(where);
        }

        return transaction;
    }
}