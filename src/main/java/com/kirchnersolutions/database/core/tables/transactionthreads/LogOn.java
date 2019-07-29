package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.UserService;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.database.sessions.Session;

import java.math.BigInteger;
import java.util.concurrent.Callable;

class LogOn implements Callable<Transaction> {

    private UserService userService;

    private volatile Transaction transaction;
    private volatile Session session;

    public LogOn(Transaction transaction, Session session, UserService userService){
        this.transaction = transaction;
        this.session = session;
        this.userService = userService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":LogOn:");
        if (transaction.getUsername().equals(new String("")) || transaction.getPassword().equals(new BigInteger("-1"))) {
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("Improper Credentials"));
        }
        try {
            Session result = LogOn(transaction, session);
            if(result == null){
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Improper Credentials"));
                return transaction;
            }
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
        } catch (Exception e) {
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
        return transaction;
    }

    private Session LogOn(Transaction transaction, Session session) throws Exception {
        try {
            User user = logon(transaction.getUsername(), transaction.getPassword(), session.getType());
            if(user == null){

                transaction.setUserIndex(new BigInteger("-1"));
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid credentials"));
                return null;
            }
            BigInteger index = new BigInteger(user.getDetail(new String("index")).toString());
            session.setUser(user);
            transaction.setUserIndex(index);
            transaction.setSuccessfull(true);
            transaction.setFinishTime(System.currentTimeMillis());
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            transaction.setUserIndex(new BigInteger("-1"));
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String(e.getMessage()));
            throw e;
        }
    }

    private User logon(String userName, BigInteger Password, String type) throws Exception{
        return userService.logOn(userName, Password, type);

        //updateTime();
        //return new BigInteger(user.getDetail(new String("index")).toString());
    }
}