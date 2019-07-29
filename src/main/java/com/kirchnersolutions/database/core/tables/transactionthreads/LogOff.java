package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;

import java.math.BigInteger;
import java.util.concurrent.Callable;

class LogOff implements Callable<Transaction> {

    private SessionService sessionService;

    private volatile Transaction transaction;
    private volatile Session session;

    public LogOff(Transaction transaction, Session session, SessionService sessionService){
        this.transaction = transaction;
        this.session = session;
        this.sessionService = sessionService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":LogOff:");
        if (transaction.getUsername().equals(new String("")) || transaction.getPassword().equals(new BigInteger("-1"))) {
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("Improper Credentials"));
        }
        try {
            LogOff(transaction, session, 2);
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

    private Transaction LogOff(Transaction transaction, Session session, int action) throws Exception {

        if (session.getUser() == null) {
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("User is not logged on"));
            return transaction;
        }
        try {
            sessionService.logoff(action, session);
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