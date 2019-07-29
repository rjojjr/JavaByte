package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

class CreateTable implements Callable<Transaction> {

    private TableManagerService tableManagerService;


    private volatile Transaction transaction;

    public CreateTable(Transaction transaction, TableManagerService tableManagerService){
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":CreateTable:");
        List<String> expression = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        List<String> rules = new ArrayList<>(Arrays.asList(transaction.getOperation().split(":")));
        if (rules.size() == 1 || rules.get(1).equals(new String(""))) {
            String[] fields = expression.get(3).split(";");
            try {

                if (tableManagerService.createNewTable(expression.get(2), fields, null)) {
                    transaction.setSuccessfull(true);
                    transaction.setFinishTime(System.currentTimeMillis());
                    System.out.println("here");
                } else {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("An unknown error has occured"));
                }
            } catch (Exception e) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        } else {
            String[] fields = expression.get(3).split(";");
            List<String> fieldList = new ArrayList<>(Arrays.asList(rules.get(1).split(";")));
            String[] indexs = new String[fieldList.size()];
            for (int i = 0; i < fields.length; i++) {
                indexs[i] = fieldList.get(i).toString();
            }
            try {
                if (tableManagerService.createNewTable(expression.get(2), fields, indexs)) {
                    transaction.setSuccessfull(true);
                    transaction.setFinishTime(System.currentTimeMillis());
                } else {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("An unknown error has occured"));
                }
            } catch (Exception e) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
        }
        return transaction;
    }
}