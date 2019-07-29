package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TableContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class HashTable implements Callable<byte[]> {

    private TableContainer tableContainer;

    public HashTable(TableContainer tableContainer){
        this.tableContainer = tableContainer;
    }

    @Override
    public byte[] call() throws Exception {
        Thread.currentThread().setName("Hash table " + tableContainer.getName());
        return tableContainer.hashTable();
    }
}
