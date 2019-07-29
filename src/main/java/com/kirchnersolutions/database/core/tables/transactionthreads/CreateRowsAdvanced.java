package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

class CreateRowsAdvanced implements Callable<Transaction> {

    private TableManagerService tableManagerService;

    private volatile Transaction transaction;

    public CreateRowsAdvanced(Transaction transaction, TableManagerService tableManagerService) {
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":CreateRowsAdvanced:");
        List<String> expression = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        List<String> rules = new ArrayList<>(Arrays.asList(expression.get(4).split(":")));
        if (rules.size() == 1 || rules.get(1).equals(new String(""))) {
            try {
                if (tableManagerService.createRows(transaction.getNewRows(), rules.get(0))) {
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
        } else {
            try {
                if (tableManagerService.createRows(transaction.getNewRows(), rules.get(0))) {
                    transaction.setSuccessfull(true);
                    transaction.setFinishTime(System.currentTimeMillis());
                    if (tableManagerService.createRows(transaction.getNewRows2(), rules.get(1))) {

                    } else {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("An unknown error has occured"));
                    }
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