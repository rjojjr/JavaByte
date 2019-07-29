package com.kirchnersolutions.database.core.tables;

/**
 * 2019 Kirchner Solutions
 *
 * @Author Robert Kirchner Jr.
 * <p>
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */

import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableInfo;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;


import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.Configuration.TableConfiguration;
import com.kirchnersolutions.database.core.tables.threads.*;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.TableException;
import com.kirchnersolutions.database.exceptions.TableThreadException;
import com.kirchnersolutions.database.objects.AtomicBigInteger;
import com.kirchnersolutions.utilities.ByteTools;
import com.kirchnersolutions.utilities.CryptTools;
import com.kirchnersolutions.utilities.SerialService.GeneralSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
//@DependsOn({"sysVars", "sevVars", "DatabaseObjectFactory", "GeneralSerializer", "TableConfiguration", "ThreadPoolTaskExecutor", "TableThreadService"})

@DependsOn({"debuggingService", "tableManagerService"})
public class TableContainer {

    SysVars sysVars;
    DevVars devVars;
    DebuggingService debuggingService;
    TableConfiguration tableConfiguration;
    GeneralSerializer generalSerializer;
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    TableThreadService tableThreadService;
    DatabaseObjectFactory databaseObjectFactory;

    private volatile List<TablePage> tableList = Collections.synchronizedList(new ArrayList<>());
    private File tableDir, tablePageDir, definitionFile, indexFile, nextIndexFile;
    File[] tablePageDirs = null;
    private volatile String[] definition, indexedFields;
    private volatile int tablePageCount;
    private String name;
    private volatile AtomicBigInteger nextIndex;

    /**
     * Call this constructor only to create a new table.
     *
     * @param tableDir
     * @param definition
     * @param indexedFields
     * @throws DevelopmentException
     * @throws TableException
     */
    TableContainer(File tableDir, String[] definition, String[] indexedFields, TableThreadService tableThreadService, ThreadPoolTaskExecutor threadPoolTaskExecutor,
                   GeneralSerializer generalSerializer, DevVars devVars, SysVars sysVars, TableConfiguration tableConfiguration, DatabaseObjectFactory databaseObjectFactory, DebuggingService debuggingService) throws Exception, TableException {
        this.tableThreadService = tableThreadService;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.generalSerializer = generalSerializer;
        this.databaseObjectFactory = databaseObjectFactory;
        this.devVars = devVars;
        this.debuggingService = debuggingService;
        this.sysVars = sysVars;
        this.tableConfiguration = tableConfiguration;
        this.tableDir = tableDir;
        name = new String(tableDir.getName());
        this.definition = definition;
        this.indexedFields = indexedFields;
        tablePageDir = new File(tableDir, "/TablePages");
        definitionFile = new File(tableDir, "/definition");
        indexFile = new File(tableDir, "/index");
        nextIndexFile = new File(tableDir, "/next");
        //If the table doesn't exist create new one, else throw exception.
        if (!tableDir.exists()) {
            tableDir.mkdirs();
            tablePageDir.mkdirs();
            try {
                definitionFile.createNewFile();
            } catch (Exception e) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to create table " + tableDir.getName() + " definition file. " + " Class TableContainer Method Constructor(File, STring[], String[])");
                    throw new DevelopmentException("Failed to create table " + tableDir.getName() + " definition file. " + " Class TableContainer Method Constructor(File, STring[], String[])");
                } else {
                    System.out.println("Failed to create table " + tableDir.getName() + " definition file. ");
                    throw new TableException("Failed to create table " + tableDir.getName() + " definition file. ");
                }
            }
            try {
                indexFile.createNewFile();
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to create table " + tableDir.getName() + " index file. " + " Class TableContainer Method Constructor(File, STring[], String[])"));
                System.out.println("Failed to create table " + tableDir.getName() + " index file. ");
                throw new TableException("Failed to create table " + tableDir.getName() + " index file. ");
            }
            try {
                nextIndexFile.createNewFile();
                nextIndex = new AtomicBigInteger(new BigInteger("0"));
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to create table " + tableDir.getName() + " index count file. " + " Class TableContainer Method Constructor(File, STring[], String[])"));
                System.out.println("Failed to create table " + tableDir.getName() + " index count file. ");
                throw new TableException("Failed to create table " + tableDir.getName() + " index count file. ");

            }
            try {
                byte[] bytes = generalSerializer.serialize(definition);
                ByteTools.writeBytesToFile(definitionFile, bytes);
            } catch (Exception e) {
                throw e;
            }
                /*
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to write table " + tableDir.getName() + " definition to file. " + " Class TableContainer Method Constructor(File, STring[], String[])");
                    throw new DevelopementException("Failed to write table " + tableDir.getName() + " definition to file. " + " Class TableContainer Method Constructor(File, STring[], String[])");
                } else {
                    System.out.println("Failed to write table " + tableDir.getName() + " definition to file. ");
                    throw new TableException("Failed to write table " + tableDir.getName() + " definition to file. ");
                }
            }

                 */
            try {
                byte[] bytes;
                if (indexedFields == null) {
                    bytes = new byte[1];
                    bytes[0] = -1;
                    ByteTools.writeBytesToFile(indexFile, bytes);
                } else {
                    bytes = generalSerializer.serialize(indexedFields);
                    ByteTools.writeBytesToFile(indexFile, bytes);
                }

            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to write table " + tableDir.getName() + " index to file. " + " Class TableContainer Method Constructor(File, STring[], String[])"));
                System.out.println("Failed to write table " + tableDir.getName() + " index to file. ");
                throw new TableException("Failed to write table " + tableDir.getName() + " index to file. ");
            }
            try {
                byte[] bytes = nextIndex.get().toByteArray();
                ByteTools.writeBytesToFile(nextIndexFile, bytes);
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to write table " + tableDir.getName() + " index count to file. " + " Class TableContainer Method Constructor(File, STring[], String[])"));
                System.out.println("Failed to write table " + tableDir.getName() + " index count to file. ");
                throw new TableException("Failed to write table " + tableDir.getName() + " index count to file. ");
            }
            BigInteger tablePages = new BigInteger(tableConfiguration.getInitTablePageSize());
            tablePageCount = tablePages.intValue();
            tablePageDirs = new File[tablePages.intValue()];
            //Create TablePage directories.
            for (int i = 1; i <= tablePageCount; i++) {
                tablePageDirs[i - 1] = new File(tablePageDir, sysVars.getFileSeperator() + tableDir.getName() + "-" + i);
                tablePageDirs[i - 1].mkdirs();
                tableList.add(i - 1, new TablePage(tablePageDirs[i - 1], this));
            }
            /*
            //Create and store TablePages
            for (File dir : tablePageDirs) {
                TablePage tablePage = new TablePage(dir, this);
                tableList.add(tablePage);
            }
            */
        } else {
            debuggingService.throwDevException(new DevelopmentException("Failed to create table " + tableDir.getName() + " Table already exists." + " Class TableContainer Method Constructor(File, STring[], String[])"));
            System.out.println("Failed to create table " + tableDir.getName() + " Table already exists.");
            throw new TableException("Failed to create table " + tableDir.getName() + " Table already exists.");
        }

    }

    /*
    @PostConstruct
    public void init() throws Exception {

    }



     */
    File getTableDir() {
        return tableDir;
    }

    /**
     * Initialize an existing table.
     *
     * @param tableDir
     * @throws DevelopmentException
     * @throws TableException
     */
    TableContainer(File tableDir, TableThreadService tableThreadService, ThreadPoolTaskExecutor threadPoolTaskExecutor,
                   GeneralSerializer generalSerializer, DevVars devVars, SysVars sysVars, TableConfiguration tableConfiguration, DatabaseObjectFactory databaseObjectFactory, DebuggingService debuggingService) throws DevelopmentException, TableException {
        this.tableThreadService = tableThreadService;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.databaseObjectFactory = databaseObjectFactory;
        this.generalSerializer = generalSerializer;
        this.devVars = devVars;
        this.sysVars = sysVars;
        this.debuggingService = debuggingService;
        this.tableConfiguration = tableConfiguration;
        this.tableDir = tableDir;
        this.definition = definition;
        this.indexedFields = indexedFields;
        name = new String(tableDir.getName());
        tablePageDir = new File(tableDir, "/TablePages");
        definitionFile = new File(tableDir, "/definition");
        indexFile = new File(tableDir, "/index");
        nextIndexFile = new File(tableDir, "/next");
        //If the table doesn't exist create new one, else throw exception.
        if (tableDir.exists()) {
            if (!tablePageDir.exists()) {
                debuggingService.throwDevException(new DevelopmentException("Failed to intialize table " + tableDir.getName() + " TablePages does not exist." + " Class TableContainer Method Constructor(File)"));
                System.out.println("Failed to intialize table " + tableDir.getName() + " TablePages does not exist.");
                throw new TableException("Failed to intialize table " + tableDir.getName() + " TablePages does not exist.");
            }
            if (!definitionFile.exists()) {
                debuggingService.throwDevException(new DevelopmentException("Failed to intialize table " + tableDir.getName() + " Table definition file does not exist." + " Class TableContainer Method Constructor(File)"));
                System.out.println("Failed to intialize table " + tableDir.getName() + " Table definition file does not exist.");
                throw new TableException("Failed to intialize table " + tableDir.getName() + " Table definition file does not exist.");
            } else {
                byte[] bytes = null;
                try {
                    bytes = ByteTools.readBytesFromFile(definitionFile);
                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to read table " + tableDir.getName() + " definition file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to read table " + tableDir.getName() + " definition file.");
                    throw new TableException("Failed to read table " + tableDir.getName() + " definition file.");
                }
                try {
                    definition = (String[]) generalSerializer.deserialize(bytes);
                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to deserialize table " + tableDir.getName() + " definition file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to deserialize table " + tableDir.getName() + " definition file.");
                    throw new TableException("Failed to deserialize table " + tableDir.getName() + " definition file.");
                }
            }
            if (!indexFile.exists()) {
                debuggingService.throwDevException(new DevelopmentException("Failed to intialize table " + tableDir.getName() + " Table index file does not exist." + " Class TableContainer Method Constructor(File)"));
                System.out.println("Failed to intialize table " + tableDir.getName() + " Table index file does not exist.");
                throw new TableException("Failed to intialize table " + tableDir.getName() + " Table index file does not exist.");
            } else {
                byte[] bytes = null;
                try {
                    bytes = ByteTools.readBytesFromFile(indexFile);
                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to read table " + tableDir.getName() + " index file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to read table " + tableDir.getName() + " index file.");
                    throw new TableException("Failed to read table " + tableDir.getName() + " index file.");
                }
                try {
                    if (bytes[0] == -1) {
                        indexedFields = null;
                    } else {
                        indexedFields = (String[]) generalSerializer.deserialize(bytes);
                    }

                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to deserialize table " + tableDir.getName() + " index file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to deserialize table " + tableDir.getName() + " index file.");
                    throw new TableException("Failed to deserialize table " + tableDir.getName() + " index file.");
                }
            }
            if (!nextIndexFile.exists()) {
                debuggingService.throwDevException(new DevelopmentException("Failed to intialize table " + tableDir.getName() + " Table index count file does not exist." + " Class TableContainer Method Constructor(File)"));
                System.out.println("Failed to intialize table " + tableDir.getName() + " Table index count file does not exist.");
                throw new TableException("Failed to intialize table " + tableDir.getName() + " Table index count file does not exist.");
            } else {
                byte[] bytes = null;
                try {
                    bytes = ByteTools.readBytesFromFile(nextIndexFile);
                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to read table " + tableDir.getName() + " index count file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to read table " + tableDir.getName() + " index count file.");
                    throw new TableException("Failed to read table " + tableDir.getName() + " index count file.");
                }
                try {
                    nextIndex = new AtomicBigInteger(new BigInteger(bytes));
                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to deserialize table " + tableDir.getName() + " index count file." + " Class TableContainer Method Constructor(File)"));
                    System.out.println("Failed to deserialize table " + tableDir.getName() + " index count file.");
                    throw new TableException("Failed to deserialize table " + tableDir.getName() + " index count file.");
                }
            }
            tablePageCount = tablePageDir.listFiles().length;
            tablePageDirs = new File[tablePageCount];
            //Load TablePage directories.
            for (int i = 1; i <= tablePageCount; i++) {
                tablePageDirs[i - 1] = new File(tablePageDir, sysVars.getFileSeperator() + tableDir.getName() + "-" + i);
                if (tablePageDirs[i - 1].exists()) {
                    TablePage tablePage = new TablePage(tablePageDirs[i - 1], this);
                    LoadTablePage(tablePage);
                    tableList.add(i - 1, tablePage);
                } else {
                    int t = i - 1;
                    debuggingService.throwDevException(new DevelopmentException("Table " + tableDir.getName() + " page node " + t + " is missing or corrupt. Class TableContainer Method Constructor(File, STring[], String[])"));
                    System.out.println("Table " + tableDir.getName() + " page node " + t + " is missing or corrupt.");
                    throw new TableException("Table " + tableDir.getName() + " page node " + t + " is missing or corrupt.");
                }
            }
            /*
            //Load and store TablePages
            for (File dir : tablePageDirs) {
                TablePage tablePage = new TablePage(dir, this);
                threadPoolTaskExecutor.execute(new LoadTablePage(tablePage));
                tableList.add(tablePage);
            }
             */
        } else {
            debuggingService.throwDevException(new DevelopmentException("Failed to initialize table " + tableDir.getName() + " Table does not exists." + " Class TableContainer Method Constructor(File, STring[], String[])"));
            System.out.println("Failed to initialize table " + tableDir.getName() + " Table does not exists.");
            throw new TableException("Failed to initialize table " + tableDir.getName() + " Table does not exists.");
        }
    }

    /**
     * Returns table name.
     *
     * @return
     */
    public String getName() {
        return new String(name);
    }

    /**
     * Searches the TablePage with the given index.
     *
     * @param request
     * @param tablePageIndex
     * @return
     * @throws DevelopmentException
     * @throws TableException
     */
    List<Map<String, String>> simpleSearchTablePage(Map<String, String> request, int tablePageIndex) throws DevelopmentException, TableException {
        return SimpleSearchTablePage(request, tablePageIndex);
    }

    /**
     * Only finds howMany results that match request from all TablePages.
     *
     * @param request
     * @param howMany
     * @return
     * @throws DevelopmentException
     * @throws TableException
     */
    List<Map<String, String>> simpleSearchTablePageSoMany(Map<String, String> request, int howMany) throws DevelopmentException, TableException {
        return SimpleSearchAllTablePageHowMany(request, howMany);
    }

    /**
     * Searches all TablePages.
     *
     * @param request
     * @return
     * @throws DevelopmentException
     * @throws TableException
     */
    List<Map<String, String>> simpleSearchAllTablePage(Map<String, String> request) throws DevelopmentException, TableException {
        return SimpleSearchAllTablePage(request);
    }

    /**
     * Blocks until all TablePages are loading then returns true.
     *
     * @return
     */
    boolean isTablesLoading() {
        return tablesLoading();
    }

    /**
     * Reloads the table with the given index.
     *
     * @param tablePageIndex
     * @throws DevelopmentException
     * @throws TableException
     */
    void reloadTable(int tablePageIndex) throws DevelopmentException, TableException {
        ReloadTablePage(tablePageIndex);
    }

    void reloadEntireTable() throws Exception {
        for (int i = 0; i < tableList.size(); i++) {
            ReloadTablePage(i);
        }
    }

    synchronized StompTableInfo getTableStats() {
        StompTableInfo response = new StompTableInfo();
        response.setDefinition(getDefinition());
        if (indexedFields != null) {
            response.setIndexedfields(getIndexedFields());
        } else {

        }

        response.setRowcount(getRowCount().toString());
        response.setTablepagecount(tableList.size() + "");
        response.setTablename(getName());
        try {
            BigInteger index = new BigInteger(ByteTools.readBytesFromFile(nextIndexFile));
            response.setCurrentindex(index.toString());
        } catch (Exception e) {
            response.setCurrentindex("Error");
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Returns array of indexed fields.
     *
     * @return
     */
    String[] getIndexedFields() {
        return Arrays.copyOf(indexedFields, indexedFields.length);
    }

    /**
     * returns copy of table definition.
     *
     * @return
     */
    String[] getDefinition() {
        return Arrays.copyOf(definition, definition.length);
    }

    /**
     * Overwrites current indexes and reloads all TablePages.
     *
     * @param indexedFields
     * @throws DevelopmentException
     * @throws TableException
     */
    void setIndexes(String[] indexedFields) throws Exception, TableException {
        SetIndexes(indexedFields);
    }

    /**
     * Gets the next howMany indexes.
     *
     * @return
     */
    synchronized List<BigInteger> getNextIndexes(int howMany) throws Exception {
        return GetNextIndex(howMany);
    }

    /**
     * Returns the old values that were changed. Returns null if operation failed and rollback was performed.
     *
     * @param changes
     * @return
     * @throws Exception
     */
    Map<BigInteger, Map<String, String>> editFields(Map<BigInteger, Map<String, String>> changes) throws Exception {
        return EditFields(changes);
    }

    /**
     * Deletes all rows with indexes contained in the list.
     *
     * @param indexes
     * @return
     * @throws Exception
     */
    Map<BigInteger, Map<String, String>> deleteRows(List<BigInteger> indexes) throws Exception {
        return DeleteRows(indexes);
    }

    synchronized List<Map<BigInteger, Map<String, String>>> deleteTable() throws Exception {
        while (isTablesLoading()) {

        }
        return DeleteTable();
    }

    List<Map<String, String>> getRowsFromIndexList(List<BigInteger> indexes) throws Exception {
        return GetRowsFromIndexList(indexes);
    }

    synchronized void setNextIndex(BigInteger index) throws Exception {
        SetNextIndex(index);
    }

    /**
     * Returns table page number index should be located in.
     *
     * @param index
     * @return
     */
    int tableNumberContainingIndex(BigInteger index) {
        return tableIndexIsIn(index);
    }

    /**
     * Creates the rows supplied.
     * Throws exception if operation fails and rollback takes place.
     *
     * @param newRowFields
     * @return
     * @throws Exception
     */
    AtomicBoolean createRows(List<Map<String, String>> newRowFields) throws Exception {
        return CreateRows(newRowFields);
    }

    List<BigInteger> createRowsReturnIndexes(List<Map<String, String>> newRowFields) throws Exception {
        return CreateRowsReturnIndexs(newRowFields);
    }

    List<byte[]> getTableCSV(String delim) throws Exception {
        return GetTableCSV(delim);
    }

    public byte[] hashTable() throws Exception {
        return HashTable();
    }

    BigInteger getRowCount() {
        BigInteger count = new BigInteger("0");
        for (TablePage table : tableList) {
            count = count.add(table.getRowCount());
        }
        return count;
    }

    private void LoadTablePage(TablePage tablePage) {
        threadPoolTaskExecutor.execute(new LoadTablePage(tablePage));
    }

    private List<byte[]> GetTableCSV(String delim) throws Exception {
        List<byte[]> csvs = new ArrayList<>();
        Future<byte[]>[] futures = new Future[tableList.size()];
        int count = 0;
        for (TablePage page : tableList) {
            futures[count] = threadPoolTaskExecutor.submit(new PageCSV(page, delim));
            count++;
        }
        for (Future<byte[]> future : futures) {
            csvs.add(future.get());
        }
        return csvs;
    }

    private synchronized byte[] HashTable() throws Exception {
        List<byte[]> hashes = new ArrayList<>();
        Future<byte[]>[] futures = new Future[tableList.size()];
        int count = 0;
        for (TablePage page : tableList) {
            futures[count] = threadPoolTaskExecutor.submit(new HashPage(page));
            count++;
        }
        for (Future<byte[]> future : futures) {
            hashes.add(future.get());
        }
        byte[] hash = new byte[1];
        boolean first = true;
        for (byte[] bytes : hashes) {
            if (first) {
                hash = CryptTools.getSHA256(new String(bytes, "UTF-8"));
                first = false;
            } else {
                hash = CryptTools.getSHA256(new String(hash, "UTF-8") + new String(bytes, "UTF-8"));
            }
        }
        return hash;
    }

    private AtomicBoolean CreateRows(List<Map<String, String>> newRowFields) throws Exception {
        List<BigInteger> indexes = GetNextIndex(newRowFields.size());
        Map<BigInteger, Map<String, String>> newRows = new HashMap<>();
        int count = 0;
        for (Map<String, String> row : newRowFields) {
            if (row == null || row.size() != definition.length) {
                debuggingService.throwDevException(new DevelopmentException("Failed to create rows in table " + this.name + " invalid or corrupt request." + " Class TableContainer Method CreateRows"));
                System.out.println("Failed to create rows in table " + this.name + " invalid or corrupt request.");
                throw new TableException("Failed to create rows in table " + this.name + " invalid or corrupt request.");
            }
            newRows.put(indexes.get(count), row);
        }
        tablesLoading();
        return tableThreadService.createRows(tableList, newRows);
    }

    private List<BigInteger> CreateRowsReturnIndexs(List<Map<String, String>> newRowFields) throws Exception {
        List<BigInteger> indexes = GetNextIndex(newRowFields.size());
        Map<BigInteger, Map<String, String>> newRows = new HashMap<>();
        int count = 0;
        for (Map<String, String> row : newRowFields) {
            if (row == null || row.size() != definition.length) {
                debuggingService.throwDevException(new DevelopmentException("Failed to create rows in table " + this.name + " invalid or corrupt request." + " Class TableContainer Method CreateRows"));
                System.out.println("Failed to create rows in table " + this.name + " invalid or corrupt request.");
                throw new TableException("Failed to create rows in table " + this.name + " invalid or corrupt request.");
            }
            newRows.put(indexes.get(count), row);
        }
        tablesLoading();
        AtomicBoolean future = tableThreadService.createRows(tableList, newRows);
        if (future.get()) {
            return null;
        }
        return indexes;
    }

    private Map<BigInteger, Map<String, String>> DeleteRows(List<BigInteger> indexes) throws Exception {
        tablesLoading();
        try {
            Map<BigInteger, Map<String, String>> future = tableThreadService.deleteRows(indexes, tableList);
            return future;
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Failed to delete rows in table " + this.name + " " + e.getMessage() + " Class TableContainer Method DeleteRows"));
            System.out.println("Failed to delete rows in table " + this.name + " " + e.getMessage());
            throw new TableThreadException("Failed to delete rows in table " + this.name + " " + e.getMessage());
        }
    }

    private synchronized List<BigInteger> GetNextIndex(int howMany) throws Exception {
        List<BigInteger> indexes = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            indexes.add(nextIndex.incrementAndGet());
        }
        writeIndexToDisk();
        return indexes;
    }

    private synchronized void SetNextIndex(BigInteger index) throws Exception {
        nextIndex = new AtomicBigInteger(index);
        writeIndexToDisk();
    }

    private synchronized void writeIndexToDisk() throws Exception {
        try {
            ByteTools.writeBytesToFile(nextIndexFile, nextIndex.get().toByteArray());
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Failed to write table " + tableDir.getName() + " index to file." + " Class TableContainer Method GetNextIndex"));
            System.out.println("Failed to delete rows in table " + this.name + " " + e.getMessage());
            throw new TableException("Failed to write table " + tableDir.getName() + " index to file.");
        }
    }

    private Map<BigInteger, Map<String, String>> EditFields(Map<BigInteger, Map<String, String>> changes) throws Exception {
        tablesLoading();
        Map<BigInteger, Map<String, String>> future = tableThreadService.editFields(tableList, changes);
        return future;
    }

    private void SetIndexes(String[] indexedFields) throws Exception {
        this.indexedFields = indexedFields;
        try {
            byte[] bytes = generalSerializer.serialize(indexedFields);
            ByteTools.writeBytesToFile(indexFile, bytes);
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Failed to write table " + tableDir.getName() + " index to file. " + " Class TableContainer Method SetIndexes"));
            System.out.println("Failed to write table " + tableDir.getName() + " index to file. ");
            throw new TableException("Failed to write table " + tableDir.getName() + " index to file.");
        }
        for (int i = 0; i < tableList.size(); i++) {
            ReloadTablePage(i);
        }
    }

    private boolean tablesLoading() {
        for (TablePage tablePage : tableList) {
            while (tablePage.isLoading()) {

            }
        }
        return true;
    }

    private List<Map<String, String>> GetRowsFromIndexList(List<BigInteger> indexes) throws Exception {
        List<Map<String, String>> future = tableThreadService.getRowsFromIndexList(tableList, indexes);
        return future;
    }

    private void ReloadTablePage(int index) throws DevelopmentException, TableException {
        TablePage tablePage = (TablePage) tableList.get(index);
        try {
            tablePage = new TablePage(new File(tablePageDir, sysVars.getFileSeperator() + tableDir.getName() + "-" + (index + 1)), this);
            LoadTablePage(tablePage);
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Failed to reload tablepage " + tableDir.getName() + "-" + (index + 1) + " " + e.getMessage() + " Class TableContainer Method ReloadTablePage"));
            System.out.println("Failed to reload tablepage " + tableDir.getName() + "-" + (index + 1) + " " + e.getMessage());
            throw new TableException("Failed to reload tablepage " + tableDir.getName() + "-" + (index + 1) + " " + e.getMessage());
        }
    }

    private List<Map<String, String>> SimpleSearchAllTablePage(Map<String, String> request) throws DevelopmentException, TableException {
        tablesLoading();
        List<Map<String, String>>[] results = new ArrayList[tableList.size()];
        Future<List<Map<String, String>>>[] futures = new Future[tableList.size()];
        int count = 0;
        for (TablePage table : tableList) {
            while (table.isLoading()) {

            }
            futures[count] = threadPoolTaskExecutor.submit(new SimpleSearchTablePage(table, request, devVars));
            count++;
        }
        count = 0;
        for (Future<List<Map<String, String>>> future : futures) {
            try {
                results[count] = future.get();
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage() + " Class TableContainer Method SimpleSearchTablePage"));
                System.out.println("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage());
                throw new TableException("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage());
            }
            count++;
        }
        List<Map<String, String>> result = combineRowLists(results);
        Collections.sort(result, new MapIndexReverseComparator());
        return result;
    }

    private int tableIndexIsIn(BigInteger index) {
        BigDecimal n = new BigDecimal(tablePageDir.length());
        BigDecimal x = new BigDecimal(index);
        BigDecimal t1 = x.divide(n);
        BigDecimal t2 = x.multiply(n).subtract(x);
        BigDecimal t3 = t2.divide(n);
        BigDecimal t4 = t1.add(t1);
        return t4.toBigInteger().intValue();
    }

    private List<Map<String, String>> SimpleSearchAllTablePageHowMany(Map<String, String> request, int howMany) throws DevelopmentException, TableException {
        tablesLoading();
        List<Map<String, String>>[] results = new ArrayList[tableList.size()];
        Future<List<Map<String, String>>>[] futures = new Future[tableList.size()];
        int count = 0;
        AtomicInteger found = new AtomicInteger();
        found.set(0);
        for (TablePage table : tableList) {
            while (table.isLoading()) {

            }
            futures[count] = threadPoolTaskExecutor.submit(new HowManyTableSearch(found, howMany, table, request));
            count++;
        }
        count = 0;
        for (Future future : futures) {
            try {
                results[count] = (List<Map<String, String>>) future.get();
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage() + " Class TableContainer Method SimpleSearchAllTablePageHowMany"));
                System.out.println("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage());
                throw new TableException("Failed to search tablepage " + tableDir.getName() + "-" + (count + 1) + " " + e.getMessage());
            }
            count++;
        }
        List<Map<String, String>> result = combineRowLists(results);
        Collections.sort(result, new MapIndexReverseComparator());
        return result;
    }

    private List<Map<String, String>> SimpleSearchTablePage(Map<String, String> request, int tablePageIndex) throws DevelopmentException, TableException {
        tablesLoading();
        List<Map<String, String>> result = null;
        Future<List<Map<String, String>>> future = threadPoolTaskExecutor.submit(new SimpleSearchTablePage((TablePage) tableList.get(tablePageIndex), request, devVars));
        try {
            result = future.get();
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Failed to search tablepage " + tableDir.getName() + "-" + (tablePageIndex + 1) + " " + e.getMessage() + " Class TableContainer Method SimpleSearchTablePage"));
            System.out.println("Failed to search tablepage " + tableDir.getName() + "-" + (tablePageIndex + 1) + " " + e.getMessage());
            throw new TableException("Failed to search tablepage " + tableDir.getName() + "-" + (tablePageIndex + 1) + " " + e.getMessage());
        }
        return result;
    }

    private List<Map<BigInteger, Map<String, String>>> DeleteTable() throws Exception {
        List<Map<BigInteger, Map<String, String>>> oldValues = new ArrayList<>();
        for (TablePage tablePage : tableList) {
            oldValues.add(tableThreadService.deleteTable(tablePage));
            tablePage = null;
        }
        tableList = null;
        if (indexFile.exists()) {
            indexFile.delete();
        }
        if (nextIndexFile.exists()) {
            nextIndexFile.delete();
        }
        if (tablePageDir.exists()) {
            tablePageDir.delete();
        }
        if (definitionFile.exists()) {
            definitionFile.delete();
        }
        if (tableDir.exists()) {
            tableDir.delete();
        }
        return oldValues;
    }

    private List<Map<String, String>> combineRowLists(List<Map<String, String>>[] lists) {
        List<Map<String, String>> result = new ArrayList<>();
        for (List<Map<String, String>> list : lists) {
            for (Map<String, String> row : list) {
                result.add(row);
            }
        }
        return result;
    }
}