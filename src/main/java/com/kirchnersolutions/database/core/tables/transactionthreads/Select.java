package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.objects.Transaction;

import java.util.*;
import java.util.concurrent.Callable;

class Select implements Callable<Transaction> {

    private TransactionThreadService transactionThreadService;

    private volatile Transaction transaction;

    public Select(Transaction transaction, TransactionThreadService transactionThreadService){
        this.transaction = transaction;
        this.transactionThreadService = transactionThreadService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":Select:");
        String expression = transaction.getOperation();
        if (expression.contains("JOIN")) {
            int ind = expression.indexOf("ALSO");
            String temp = expression.replace(" ALSO ", "/");
            List<String> joinParts = new ArrayList<>(Arrays.asList(temp.split("/")));
            List<String> parts = new ArrayList<>(Arrays.asList(joinParts.get(0).split(" ")));
            String selectString = parts.get(3).split(":")[0];
            List<String> select = new ArrayList<>(Arrays.asList(selectString.split(";")));
            int howMany = Integer.parseInt(parts.get(1).toString());
            String whereString = parts.get(5).split(":")[0];
            List<String> where = new ArrayList<>(Arrays.asList(selectString.split(";")));
            Map<String, String> whereMap = new HashMap<>();
            for (String field : where) {
                List<String> temp2 = new ArrayList<>(Arrays.asList(field.split("=")));
                if (temp2.size() != 2) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("Invalid argument format"));
                    return transaction;
                }
                whereMap.put(temp2.get(0), temp2.get(1));
            }
            transaction.setWhere(new HashMap<>(whereMap));
            List<String> parts2 = new ArrayList<>(Arrays.asList(joinParts.get(1).split(" ")));
            List<String> tableNames = new ArrayList<>();
            tableNames.add(parts.get(1));
            tableNames.add(parts2.get(0));
            List<String> secondSelect = new ArrayList<>(Arrays.asList(parts2.get(1).split(";")));
            for(String field : secondSelect){
                select.add(field);
            }
            transaction.setSelect(new ArrayList<>(select));
            secondSelect = new ArrayList<>(Arrays.asList(parts2.get(3).split("=")));
            if (secondSelect.size() != 2) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid argument format"));
                return transaction;
            }
            whereMap = new HashMap<>();
            whereMap.put(secondSelect.get(0), secondSelect.get(1));
            transaction.setWhere2(new HashMap<>(whereMap));
            try {
                transaction = transactionThreadService.selectAdvanced(transaction, tableNames);
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
            List<String> parts = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
            int howMany = 0;
            String selectString = parts.get(3).split(":")[0];
            List<String> select = new ArrayList<>(Arrays.asList(selectString.split(";")));
            transaction.setSelect(new ArrayList<>(select));
            String whereString = parts.get(5).split(":")[0];
            List<String> where = new ArrayList<>(Arrays.asList(selectString.split(";")));
            Map<String, String> whereMap = new HashMap<>();
            for (String field : where) {
                List<String> temp = new ArrayList<>(Arrays.asList(field.split("=")));
                if (temp.size() != 2) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("Invalid argument format"));
                    return transaction;
                }
                whereMap.put(temp.get(0), temp.get(1));
            }
            transaction.setWhere(whereMap);
            try {
                select = new ArrayList<>();
                select.add(parts.get(1));
                transaction = transactionThreadService.selectAdvanced(transaction, select);
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