package com.kirchnersolutions.database.core.tables;
/**
 * 2019 Kirchner Solutions
 *
 * @Author Robert Kirchner Jr.
 * <p>
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */

import com.kirchnersolutions.database.core.tables.transactionthreads.TransactionThreadService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.DatabaseResults;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@ApplicationScope
@DependsOn({"transactionRepository", "databaseObjectFactory", "tableManagerService"})
public class TransactionService {

    @Autowired
    private DatabaseObjectFactory databaseObjectFactory;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private volatile TransactionRepository transactionRepository;
    @Autowired
    private TransactionThreadService transactionThreadService;

    public TransactionService() throws Exception {

    }

    /**
     * Submited transaction is processed.
     * Returns byte[] of length 1 if transaction fails.
     * @param transaction
     * @param session
     * @return
     * @throws Exception
     */
    public byte[] submitTransaction(Transaction transaction, Session session) throws Exception {
        return SubmitTransaction(transaction, session);
    }

    public List<Transaction> getTransactions(String key, String value) throws Exception{
        return transactionRepository.getTransactionObjectsByKey(key, value);
    }

    public List<Transaction> getTransactions(Map<String, String> request) throws Exception{
        return transactionRepository.getTransactionObjectsByKeys(request, 0);
    }

    public List<Map<String, String>> getTransactionRows(Map<String, String> request, int howMany) throws Exception{
        return transactionRepository.getTransactionRows(request, howMany);
    }

    private byte[] SubmitTransaction(Transaction transaction, Session session) throws Exception {
        ParseTransaction(transaction, session);
        DatabaseResults result = new DatabaseResults();
        transactionRepository.submitTransaction(transaction);

        if (transaction.isSuccessfull()) {

            String operation = transaction.getOperation();
            if(transaction.getResults() == null || transaction.getResults().isEmpty()){
                result.setMessage("No Result");
                result.setSuccess(true);
            }
            if (operation.equals(new String("LOGON"))) {
                return databaseObjectFactory.databaseSerialFactory((session.getUser()));
            }
            result.setMessage("Done");
                result.setResults(transaction.getResults());
                result.setSuccess(true);
            return databaseObjectFactory.databaseSerialFactory(result);
        }
        result.setMessage(transaction.getFailMessage());
        result.setSuccess(false);
        return databaseObjectFactory.databaseSerialFactory(result);
    }

    private Transaction ParseTransaction(Transaction transaction, Session session) throws Exception {
        transaction = assignID(transaction);
        String operation = transaction.getOperation();

        if (operation.equals(new String("LOGON"))) {
            return transactionThreadService.logOn(transaction, session);
        } else if (operation.equals(new String("LOGOFF"))) {
            return transactionThreadService.logOff(transaction, session);
        } else {
            List<String> expression = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
            if (expression.get(0).equals(new String("CREATE"))) {
                if (expression.get(1).equals(new String("TABLE"))) {
                    if(session.getUser().getDetail(new String("admin")).equals(new String("true"))){
                        return transactionThreadService.createTable(transaction);
                    }
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("No privilege"));
                    return transaction;
                }
                if (expression.get(1).equals(new String("ROWS"))) {
                    if (expression.get(2).equals(new String("ADVANCED"))) {
                        return transactionThreadService.createRowsAdvanced(transaction);
                    } else {
                        return transactionThreadService.createRows(transaction);
                    }
                }
            }
            if (expression.get(0).equals(new String("SELECT"))) {
                if (expression.get(1).equals(new String("ADVANCED"))) {
                    List<String> tableNames = new ArrayList<>();
                    List<String> temp = new ArrayList<>(Arrays.asList(expression.get(2).split(":")));
                    if (temp.size() == 1 || temp.get(1).equals("")) {
                        tableNames.add(temp.get(0));
                    } else {
                        tableNames = new ArrayList<>(temp);
                    }
                    return transactionThreadService.selectAdvanced(transaction, tableNames);
                } else {
                    return transactionThreadService.select(transaction);
                }
            }
            if (expression.get(0).equals(new String("PUT"))) {
                if (expression.get(1).equals(new String("ADVANCED"))) {
                    return transactionThreadService.putAdvanced(transaction);
                } else {
                    return transactionThreadService.put(transaction);
                }
            }
            if (expression.get(0).equals(new String("DELETE"))) {
                if (expression.get(1).equals(new String("ADVANCED"))) {
                    return transactionThreadService.deleteAdvanced(transaction);
                } else if (expression.get(1).equals(new String("FROM"))) {
                    return transactionThreadService.delete(transaction);
                } else if (expression.get(1).equals(new String("TABLE"))) {
                    return transactionThreadService.deleteTable(transaction);
                }
            }
        }
        return transaction;
    }

    private Transaction assignID(Transaction transaction) throws Exception {
        try {
            transaction.setTransactionID(tableManagerService.getTransactionID());
            return transaction;
        } catch (Exception e) {
            transaction.setTransactionID(new BigInteger("-1"));
            transaction.setSuccessfull(false);
            transaction.setFinishTime(System.currentTimeMillis());
            transaction.setFailMessage(new String("Failed to get new Transaction ID " + e.getMessage()));
            throw e;
        }
    }
}