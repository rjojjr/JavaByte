package com.kirchnersolutions.database.core.tables.transactionthreads;

import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.objects.Transaction;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

class CreateRows  implements Callable<Transaction> {


    private TableManagerService tableManagerService;

    private volatile Transaction transaction;

    public CreateRows(Transaction transaction, TableManagerService tableManagerService) {
        this.transaction = transaction;
        this.tableManagerService = tableManagerService;
    }

    @Override
    public Transaction call() throws Exception {
        Thread.currentThread().setName("Transaction " + transaction.getTransactionID() + ":CreateRows:");
        List<String> expression = new ArrayList<>(Arrays.asList(transaction.getOperation().split(" ")));
        if (!expression.get(4).contains("ALSO")) {
            List<String> rules = new ArrayList<>(Arrays.asList(expression.get(4).split(":")));
            List<Map<String, String>> newRows = new ArrayList<>();
            for (String string : rules) {
                List<String> values = new ArrayList<>(Arrays.asList(string.split(";")));
                try {
                    if (tableManagerService.getTableDefinition(expression.get(3)).length != values.size()) {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("Invalid Field arguments length"));
                        return transaction;
                    }
                } catch (Exception e) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String(e.getMessage()));
                    throw e;
                }
                Map<String, String> row = new HashMap<>();
                for (String arg : values) {
                    List<String> rowField = new ArrayList<>(Arrays.asList(arg.split(".")));
                    if (rowField.size() != 2) {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("Invalid Field argument format"));
                        return transaction;
                    }
                    row.put(rowField.get(0), rowField.get(1));
                }
                newRows.add(row);
            }
            transaction.setNewRows(newRows);
            try {
                if (!tableManagerService.createRows(newRows, expression.get(3))) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("An unknown error has occured"));
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
        } else {
            List<String> tableArgs = new ArrayList<>(Arrays.asList(expression.get(4).split(" ALSO ")));
            if (tableArgs.size() != 2) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid table argument format"));
                return transaction;
            }
            List<String> rules = new ArrayList<>(Arrays.asList(tableArgs.get(0).split(":")));
            List<Map<String, String>> newRows = new ArrayList<>();
            for (String string : rules) {
                List<String> values= new ArrayList<>(Arrays.asList(string.split(";")));
                try {
                    if (tableManagerService.getTableDefinition(expression.get(3)).length != values.size()) {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("Invalid Field arguments length"));
                        return transaction;
                    }
                } catch (Exception e) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String(e.getMessage()));
                    throw e;
                }
                Map<String, String> row = new HashMap<>();
                for (String arg : values) {
                    List<String> rowField = new ArrayList<>(Arrays.asList(arg.split(".")));
                    if (rowField.size() != 2) {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("Invalid Field argument format"));
                        return transaction;
                    }
                    row.put(rowField.get(0), rowField.get(1));
                }
                newRows.add(row);
            }
            transaction.setNewRows(newRows);
            List<BigInteger> indexes = null;
            try {
                indexes = tableManagerService.createRowsReturnIndexs(newRows, expression.get(3));
                if (indexes == null) {
                    transaction.setSuccessfull(false);
                    transaction.setFinishTime(System.currentTimeMillis());
                    transaction.setFailMessage(new String("An unknown error has occured"));
                    return transaction;
                }
            } catch (Exception e) {
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String(e.getMessage()));
                throw e;
            }
            List<String> newArgs = new ArrayList<>(Arrays.asList(tableArgs.get(1).split(" ")));
            List<String> equal = new ArrayList<>(Arrays.asList(newArgs.get(1).split("=")));
            if (equal.size() != 2) {
                //Rollback
                transaction.setSuccessfull(false);
                transaction.setFinishTime(System.currentTimeMillis());
                transaction.setFailMessage(new String("Invalid Field argument format"));
                return transaction;
            }
            String t1 = equal.get(0);
            if (t1.equals(new String("index"))) {
                List<String> t2Args = new ArrayList<>(Arrays.asList(newArgs.get(2).split(":")));
                if (t2Args.size() == 1 || t2Args.get(1).equals(new String(""))) {
                    List<Map<String, String>> newRows2 = new ArrayList<>();
                    List<String> values = new ArrayList<>(Arrays.asList(t2Args.get(0).split(";")));
                    try {
                        if (tableManagerService.getTableDefinition(t2Args.get(0)).length != values.size() + 1) {
                            transaction.setSuccessfull(false);
                            transaction.setFinishTime(System.currentTimeMillis());
                            transaction.setFailMessage(new String("Invalid Field arguments length"));
                            return transaction;
                        }
                    } catch (Exception e) {
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String(e.getMessage()));
                        throw e;
                    }
                    for (BigInteger index : indexes) {
                        Map<String, String> row = new HashMap<>();
                        row.put(equal.get(1), new String(index.toString()));
                        for (String arg : values) {
                            List<String> rowField = new ArrayList<>(Arrays.asList(arg.split(".")));
                            if (rowField.size() != 2) {
                                transaction.setSuccessfull(false);
                                transaction.setFinishTime(System.currentTimeMillis());
                                transaction.setFailMessage(new String("Invalid Field argument format"));
                                return transaction;
                            }
                            row.put(rowField.get(0), rowField.get(1));
                        }
                        newRows2.add(row);
                    }
                    transaction.setNewRows2(newRows2);
                    try {
                        if (!tableManagerService.createRows(newRows, expression.get(3))) {
                            //Rollback
                            transaction.setSuccessfull(false);
                            transaction.setFinishTime(System.currentTimeMillis());
                            transaction.setFailMessage(new String("An unknown error has occured"));
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
                } else {
                    if (t2Args.size() != indexes.size()) {
                        //Rollback
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String("Invalid Field argument format"));
                        return transaction;
                    }
                    t2Args = new ArrayList<>(Arrays.asList(newArgs.get(2).split(":")));
                    List<Map<String, String>> newRows2 = new ArrayList<>();

                    List<Map<String, String>> rows;

                    try {
                        rows = tableManagerService.getRowsFromIndexList(indexes, newArgs.get(0));
                    } catch (Exception e) {
                        //rollback
                        transaction.setSuccessfull(false);
                        transaction.setFinishTime(System.currentTimeMillis());
                        transaction.setFailMessage(new String(e.getMessage()));
                        throw e;
                    }
                    int count = 0;
                    for (Map rowMap : rows) {
                        try {
                            if (tableManagerService.getTableDefinition(t2Args.get(count)).length != indexes.size() + 1) {
                                //Rollback
                                transaction.setSuccessfull(false);
                                transaction.setFinishTime(System.currentTimeMillis());
                                transaction.setFailMessage(new String("Invalid Field arguments length"));
                                return transaction;
                            }
                        } catch (Exception e) {
                            //Rollback
                            transaction.setSuccessfull(false);
                            transaction.setFinishTime(System.currentTimeMillis());
                            transaction.setFailMessage(new String(e.getMessage()));
                            throw e;
                        }
                        List<String> t = new ArrayList<>(Arrays.asList(newArgs.get(1).split("=")));
                        List<String> values = new ArrayList<>(Arrays.asList(t2Args.get(count).split(";")));
                        Map<String, String> row = new HashMap<>();
                        row.put(equal.get(1), row.get(equal.get(0)));
                        for (String arg : values) {
                            List<String> rowField = new ArrayList<>(Arrays.asList(arg.split(".")));
                            if (rowField.size() != 2) {
                                transaction.setSuccessfull(false);
                                transaction.setFinishTime(System.currentTimeMillis());
                                transaction.setFailMessage(new String("Invalid Field argument format"));
                                return transaction;
                            }
                            row.put(rowField.get(0), rowField.get(1));
                        }
                        newRows2.add(row);
                    }
                    transaction.setNewRows2(newRows2);
                    try {
                        if (!tableManagerService.createRows(newRows, newArgs.get(0))) {
                            //Rollback
                            transaction.setSuccessfull(false);
                            transaction.setFinishTime(System.currentTimeMillis());
                            transaction.setFailMessage(new String("An unknown error has occured"));
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
                }
            }
        }
        return transaction;
    }
}