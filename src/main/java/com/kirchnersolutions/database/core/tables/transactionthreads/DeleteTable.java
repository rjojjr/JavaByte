package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.core.tables.UserService;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.objects.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class DeleteTable implements Callable<Transaction> {

    private TableManagerService tableManagerService;
    private UserService userService;

    private volatile Transaction transaction;

    public DeleteTable(Transaction transaction, TableManagerService tableManagerService, UserService userService){
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
        this.userService = userService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":Delete:");
        List<String> parts =  new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        User user = userService.getUserFromRepo(transaction.getUsername());
        if(user == null){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("Invalid credentials."));
            return transaction;
        }
        try{
            transaction.setOldValues(tableManagerService.deleteTableContainer(user, parts.get(2)));
            if(transaction.getOldValues() == null) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid credentials."));
                return transaction;
            }
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
            return transaction;
        }catch (Exception e){
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
    }
}