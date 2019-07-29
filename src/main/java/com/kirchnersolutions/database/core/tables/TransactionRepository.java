package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.util.*;

//@DependsOn({"TableManagerService", "DevVars", "DatabaseObjectFactory"})
@Repository
@DependsOn("tableManagerService")
public class TransactionRepository {

    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private DatabaseObjectFactory databaseObjectFactory;

    private File transactionDir;

    public TransactionRepository() throws Exception {

    }

    @PostConstruct
    public void init() throws Exception {
        transactionDir = new File("Database");
        transactionDir = new File(transactionDir, "/Tables/Transactions");
        if (!transactionDir.exists()) {
            String[] index;
            String[] fields = new String[5];
            fields[0] = "user-i";
            fields[1] = "operation-s";
            fields[2] = "success-b";
            fields[3] = "time-i";
            fields[4] = "serial-i";
            try {
                tableManagerService.createNewTable(new String("Transactions"), fields, null);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * To be called by TransactionService after a transaction is fully processed.
     *
     * @param transaction
     * @throws Exception
     */
    void submitTransaction(Transaction transaction) throws Exception {
        SubmitTransaction(transaction);
    }

    /**
     * Returns howMany transaction table rows related to username.
     *
     * @param username
     * @param howMany
     * @return
     * @throws Exception
     */
    List<Map<String, String>> getTransactionRowsByUser(String username, int howMany) throws Exception {
        return GetTransactionRowsByUser(username, howMany);
    }

    /**
     * Returns all transactions related to given username.
     *
     * @param username
     * @return
     * @throws Exception
     */
    List<Transaction> getTransactionObjectsByUser(String username) throws Exception {
        return GetTransactionObjectsByUser(username);
    }

    /**
     * Returns howMany transactions related to given username.
     *
     * @param username
     * @param howMany
     * @return
     * @throws Exception
     */
    List<Transaction> getTransactionObjectsByUser(String username, int howMany) throws Exception {
        return GetTransactionObjectsByUser(username, howMany);
    }

    List<Transaction> getTransactionObjectsByKeys(Map<String, String> request, int howMany) throws Exception {
        return GetTransactionObjectsByKeys(request, howMany);
    }

    List<Transaction> getTransactionObjectsByKey(String key, String value, int howMany) throws Exception {
        return GetTransactionObjectsByKey(key, value, howMany);
    }

    List<Transaction> getTransactionObjectsByKey(String key, String value) throws Exception {
        return GetTransactionObjectsByKey(key, value);
    }


    List<Map<String, String>> getTransactionRows(Map<String, String> request, int howMany) throws Exception {
        return GetTransactionRows(request, howMany);
    }

    List<Transaction> getTransactions(Map<String, String> request, int howMany) throws Exception {
        return GetTransactions(request, howMany);
    }

    private void SubmitTransaction(Transaction transaction) throws Exception {
        String user = new String(transaction.getUsername());
        String time = new String(System.currentTimeMillis() + "");
        transaction.setPassword(new BigInteger("0"));
        String serial = Base64.getEncoder().encodeToString(databaseObjectFactory.databaseSerialFactory(transaction));
        String operation = transaction.getOperation();
        String success;
        if (transaction.isSuccessfull()) {
            success = new String("true");
        } else {
            success = new String("false");
        }
        List<Map<String, String>> newRow = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put(new String("user"), user);
        row.put(new String("time"), time);
        row.put(new String("serial"), serial);
        row.put(new String("operation"), operation);
        row.put(new String("success"), success);
        newRow.add(row);
        try {
            tableManagerService.createRows(newRow, new String("Transactions"));
        } catch (Exception e) {
            throw e;
        }
    }


    private List<Map<String, String>> GetTransactionRows(Map<String, String> request, int howMany) throws Exception {
        return getRows(request, howMany);
    }

    private List<Transaction> GetTransactions(Map<String, String> request, int howMany) throws Exception {
        return getTransactionsFromRows(getRows(request, howMany));
    }

    private List<Map<String, String>> GetTransactionRowsByUser(String username, int howMany) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(new String("user"), username);
        return getRows(request, howMany);
    }

    private List<Transaction> GetTransactionObjectsByUser(String username) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(new String("user"), username);
        return getTransactionsFromRows(getRows(request, 0));
    }

    private List<Transaction> GetTransactionObjectsByUser(String username, int howMany) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(new String("user"), username);
        return getTransactionsFromRows(getRows(request, howMany));
    }

    private List<Transaction> GetTransactionObjectsByKey(String key, String value) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(key, value);
        return getTransactionsFromRows(getRows(request, 0));
    }

    private List<Transaction> GetTransactionObjectsByKey(String key, String value, int howMany) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(key, value);
        return getTransactionsFromRows(getRows(request, howMany));
    }

    private List<Transaction> GetTransactionObjectsByKeys(Map<String, String> request, int howMany) throws Exception {
        return getTransactionsFromRows(getRows(request, howMany));
    }

    private List<Map<String, String>> getRows(Map<String, String> request, int howMany) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        try {
            if (howMany < 1) {
                rows = tableManagerService.searchTableAll(new String("Transactions"), request);
            } else {
                rows = tableManagerService.searchTable(new String("Transactions"), request, howMany);
            }
        } catch (Exception e) {
            throw e;
        }
        return rows;
    }

    private List<Transaction> getTransactionsFromRows(List<Map<String, String>> rows) throws Exception {
        List<Transaction> result = new ArrayList<>();

        for (Map<String, String> row : rows) {
            try {
                //System.out.println(row.get(new String("serial")));
                Transaction transaction = (Transaction) databaseObjectFactory.databaseObjectFactory(Base64.getDecoder().decode(row.get(new String("serial"))));
                result.add(transaction);
            } catch (Exception e) {
                //System.out.println(new String("UTF-8"));
                e.printStackTrace();
                throw e;
            }
        }
        return result;
    }

}