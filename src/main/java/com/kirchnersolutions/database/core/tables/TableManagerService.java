package com.kirchnersolutions.database.core.tables;

/**
 * 2019 Kirchner Solutions
 *
 * @Author Robert Kirchner Jr.
 * <p>
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.Configuration.TableConfiguration;
import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableInfo;
import com.kirchnersolutions.database.core.tables.threads.TableThreadService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.TableException;
import com.kirchnersolutions.database.exceptions.TableManagerException;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.utilities.SerialService.GeneralSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
@DependsOn({"devVars", "sysVars", "debuggingService", "configurationService"})
public class TableManagerService {

    @Autowired
    private DevVars devVars;
    @Autowired
    private SysVars sysVars;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private GeneralSerializer generalSerializer;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private TableConfiguration tableConfiguration;
    @Autowired
    private DatabaseObjectFactory databaseObjectFactory;
    @Autowired
    private TableThreadService tableThreadService;

    private File tablesDir;
    private List<TableContainer> tableList;
    private Map<String, String> tableIndexes;
    private long startTime;

    public TableManagerService() {
        startTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    @PostConstruct
    public void init() throws Exception {
        System.out.println("Loading Table Manager");
        tableIndexes = new HashMap<>();
        tableList = Collections.synchronizedList(new ArrayList<>());
        File progDir = new File("Database");
        if (!progDir.exists()) {
            progDir.mkdirs();
        }
        tablesDir = new File(progDir, "/Tables");
        if (!tablesDir.exists()) {
            tablesDir.mkdirs();
        } else {
            File[] tableContainers = tablesDir.listFiles();
            int count = 0;
            for (File table : tableContainers) {
                TableContainer tableContainer = new TableContainer(table, tableThreadService, threadPoolTaskExecutor, generalSerializer, devVars, sysVars, tableConfiguration, databaseObjectFactory, debuggingService);
                String name = tableContainer.getName();
                tableIndexes.put(name.toString(), "" + count);
                count++;
                tableList.add(tableContainer);
            }
        }
        System.out.println("Loading Table Manager Done");
    }

    public File getTablesDir() {
        return tablesDir;
    }

    /**
     * Gets an index from Transaction table to assign as transaction id.
     * @return
     * @throws Exception
     */
    BigInteger getTransactionID() throws Exception {
        TableContainer table = getTableByName(new String("Transactions"));
        BigInteger index = table.getNextIndexes(1).get(0);
        table.setNextIndex(index.subtract(new BigInteger("1")));
        return index;
    }

    /**
     * Creates new table.
     * @param name
     * @param definition
     * @param indexedFields
     * @return
     * @throws TableManagerException
     */
    public boolean createNewTable(String name, String[] definition, String[] indexedFields) throws Exception, TableManagerException, DevelopmentException {
        return CreateNewTable(name, definition, indexedFields);
    }

    public Map<BigInteger, Map<String, String>> editRows(Map<BigInteger, Map<String, String>> changes, String tableName) throws Exception {
        return EditRows(changes, tableName);
    }

    /**
     * Returns all rows from table that match request.
     * @param tableName
     * @param request
     * @return
     * @throws DevelopmentException
     * @throws TableManagerException
     * @throws TableException
     */
    public List<Map<String, String>> searchTableAll(String tableName, Map<String, String> request) throws DevelopmentException, TableManagerException, TableException {
        return SearchTableAll(tableName, request);
    }

    /**
     * Deletes rows with given indexes from given table.
     * @param indexes
     * @param tableName
     * @return
     * @throws Exception
     */
    public Map<BigInteger, Map<String, String>> deleteRows(List<BigInteger> indexes, String tableName) throws Exception {
        return DeleteRows(indexes, tableName);
    }

    /**
     * Returns howMany results from given table that match request.
     * @param tableName
     * @param request
     * @param howMany
     * @return
     * @throws DevelopmentException
     * @throws TableManagerException
     * @throws TableException
     */
    public List<Map<String, String>> searchTable(String tableName, Map<String, String> request, int howMany) throws DevelopmentException, TableManagerException, TableException {
        return SearchTable(tableName, request, howMany);
    }

    /**
     * Returns array of tables indexed fields.
     * @param tableName
     * @return
     * @throws DevelopmentException
     * @throws TableManagerException
     */
    String[] getTableIndexes(String tableName) throws DevelopmentException, TableManagerException {
        return GetTableIndexes(tableName);
    }

    /**
     * Creates given rows and returns their indexes.
     * Returns null if failed and rolled back.
     * @param newRowFields
     * @param tableName
     * @return
     * @throws Exception
     */
    public List<BigInteger> createRowsReturnIndexs(List<Map<String, String>> newRowFields, String tableName) throws Exception {
        return CreateRowsReturnIndexs(newRowFields, tableName);
    }

    /**
     * Gets rows withe the given indexes from the given table.
     * @param indexes
     * @param tableName
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getRowsFromIndexList(List<BigInteger> indexes, String tableName) throws Exception {
        return getTableByName(tableName).getRowsFromIndexList(indexes);
    }


    /**
     * Returns table definition.
     * @param tableName
     * @return
     * @throws DevelopmentException
     * @throws TableManagerException
     */
    public String[] getTableDefinition(String tableName) throws DevelopmentException, TableManagerException {
        return GetTableDefinition(tableName);
    }

    /**
     * Resets the give tables indexes.
     * @param tableName
     * @param indexedFields
     * @throws DevelopmentException
     * @throws TableManagerException
     * @throws TableException
     */
    void setNewIndex(String tableName, String[] indexedFields) throws Exception, TableManagerException, TableException {
        SetNewIndex(tableName, indexedFields);
    }

    /**
     * Returns a String list of names of all tables.
     * @return
     */
    public List<String> getTableNames() {
        return GetTableNames();
    }

    public List<Map<BigInteger, Map<String, String>>> deleteTableContainer(User user, String tableName) throws Exception {
        return DeleteTableConatiner(user, tableName);
    }

    /**
     * Returns String[n][0] = table name, String[n][1] = row count.
     * @return
     */
    public String[][] getTableStats() {
        return GetTableStats();
    }

    /**
     * Reloads all tables. Blocks until all tables are loaded.
     * @return
     * @throws Exception
     */
    public boolean reloadAllTables() throws Exception {
        return ReloadTables();
    }

    public StompTableInfo stompTableStats(String tableName) {
        return StompTableStats(tableName);
    }

    public int getTableCount() {
        return tableList.size();
    }

    /**
     * Creates rows in given table.
     * Throws exception if operation fails and rollback takes place.
     * @param newRowFields
     * @param tableName
     * @return
     * @throws Exception
     */
    public boolean createRows(List<Map<String, String>> newRowFields, String tableName) throws Exception {
        return CreateRows(newRowFields, tableName).get();
    }

    byte[] hashTableContainer(String tableName) throws Exception{
        return tableThreadService.hashTable(getTableByName(tableName));
    }

    private Map<BigInteger, Map<String, String>> EditRows(Map<BigInteger, Map<String, String>> changes, String tableName) throws Exception {
        return getTableByName(tableName).editFields(changes);
    }

    private Map<BigInteger, Map<String, String>> DeleteRows(List<BigInteger> indexes, String tableName) throws Exception {
        return getTableByName(tableName).deleteRows(indexes);
    }

    private AtomicBoolean CreateRows(List<Map<String, String>> newRowFields, String tableName) throws Exception {
        return getTableByName(tableName).createRows(newRowFields);
    }

    private List<BigInteger> CreateRowsReturnIndexs(List<Map<String, String>> newRowFields, String tableName) throws Exception {
        return getTableByName(tableName).createRowsReturnIndexes(newRowFields);
    }

    private String[][] GetTableStats() {
        String[][] result = new String[tableList.size()][2];
        int count = 0;
        for (TableContainer table : tableList) {
            result[count][0] = table.getName();
            result[count][1] = table.getRowCount().toString();
            count++;
        }
        return result;
    }

    //private Boolean

    private TableContainer getTableByName(String tableName) throws Exception {
        return tableList.get(getTableIndex(tableName));
    }

    private boolean ReloadTables() throws Exception {
        for (TableContainer table : tableList) {
            table.reloadEntireTable();
        }
        for (TableContainer table : tableList) {
            while (table.isTablesLoading()) {

            }
        }
        return true;
    }

    private StompTableInfo StompTableStats(String tableName) {
        try {
            return getTableByName(tableName).getTableStats();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void SetNewIndex(String tableName, String[] indexedFields) throws Exception, TableManagerException, TableException {
        int index = getTableIndex(tableName);
        TableContainer table = (TableContainer) tableList.get(index);
        if (table == null) {
            debuggingService.throwDevException(new DevelopmentException("Failed to get table " + tableName + " table list may be corrupt. Class TableManagerService Method SetNewIndex"));
            System.out.println("Failed to get table " + tableName + " table list may be corrupt.");
            throw new TableManagerException("Failed to get table " + tableName + " table list may be corrupt.");
        } else {
            table.setIndexes(indexedFields);
        }
    }

    private String[] GetTableDefinition(String tableName) throws DevelopmentException, TableManagerException {
        int index = getTableIndex(tableName);
        TableContainer table = (TableContainer) tableList.get(index);
        if (table == null) {
            debuggingService.throwDevException(new DevelopmentException("Failed to get table " + tableName + " table list may be corrupt. Class TableManagerService Method GetTableDefinition"));
            System.out.println("Failed to get table " + tableName + " table list may be corrupt.");
            throw new TableManagerException("Failed to get table " + tableName + " table list may be corrupt.");
        } else {
            return table.getDefinition();
        }
    }

    private String[] GetTableIndexes(String tableName) throws DevelopmentException, TableManagerException {
        int index = getTableIndex(tableName);
        TableContainer table = (TableContainer) tableList.get(index);
        if (table == null) {
            debuggingService.throwDevException(new DevelopmentException("Failed to get table " + tableName + " table list may be corrupt. Class TableManagerService Method GetTableIndexes"));
            System.out.println("Failed to get table " + tableName + " table list may be corrupt.");
            throw new TableManagerException("Failed to get table " + tableName + " table list may be corrupt.");
        } else {
            return table.getIndexedFields();
        }
    }

    private List<Map<String, String>> SearchTable(String tableName, Map<String, String> request, int howMany) throws DevelopmentException, TableManagerException, TableException {
        int index = getTableIndex(tableName);
        TableContainer table = (TableContainer) tableList.get(index);
        if (table == null) {
            debuggingService.throwDevException(new DevelopmentException("Failed to get table " + tableName + " table list may be corrupt. Class TableManagerService Method SearchTableAll"));
            System.out.println("Failed to get table " + tableName + " table list may be corrupt.");
            throw new TableManagerException("Failed to get table " + tableName + " table list may be corrupt.");
        } else {
            return table.simpleSearchTablePageSoMany(request, howMany);
        }
    }

    private List<Map<BigInteger, Map<String, String>>> DeleteTableConatiner(User user, String tableName) throws Exception {
        if (user.getDetail(new String("admin")).equals(new String("true"))) {
            TableContainer table = tableList.get(getTableIndex(tableName));
            List<Map<BigInteger, Map<String, String>>> oldValues = table.deleteTable();
            table = null;
            TrimTableList();
            return oldValues;
        }
        return null;
    }

    private synchronized void TrimTableList() {
        List<TableContainer> temp = Collections.synchronizedList(new ArrayList<>(tableList));
        List<TableContainer> newList = Collections.synchronizedList(new ArrayList<>());
        for (TableContainer table : temp) {
            if (table != null) {
                newList.add(table);
            }
        }
        tableList = Collections.synchronizedList(new ArrayList<>(newList));
    }

    private boolean CreateNewTable(String name, String[] definition, String[] indexedFields) throws Exception, TableManagerException, DevelopmentException {
        if (tableIndexes.containsKey(name)) {
            debuggingService.throwDevException(new DevelopmentException("Failed to create table " + name + " table already exists. Class TableManagerService Method CreateNewTable"));
            System.out.println("Failed to create table " + name + " table already exists.");
            throw new TableManagerException("Failed to create table " + name + " table already exists.");
        } else {
            TableContainer newTable = new TableContainer(new File(tablesDir, "/" + name.toString()), definition, indexedFields, tableThreadService, threadPoolTaskExecutor, generalSerializer, devVars, sysVars, tableConfiguration, databaseObjectFactory, debuggingService);
            //TableContainer newTable = new TableContainer(new File(tablesDir, "/" + name.toString()), definition, indexedFields);
            tableIndexes.put(name, "" + tableList.size());
            tableList.add(newTable);
            return true;
        }
    }

    private List<Map<String, String>> SearchTableAll(String tableName, Map<String, String> request) throws DevelopmentException, TableManagerException, TableException {
        int index = getTableIndex(tableName);
        TableContainer table = (TableContainer) tableList.get(index);
        if (table == null) {
            debuggingService.throwDevException(new DevelopmentException("Failed to get table " + tableName + " table list may be corrupt. Class TableManagerService Method SearchTableAll"));
            System.out.println("Failed to get table " + tableName + " table list may be corrupt.");
            throw new TableManagerException("Failed to get table " + tableName + " table list may be corrupt.");
        } else {
            return table.simpleSearchAllTablePage(request);
        }
    }

    private List<String> GetTableNames() {
        List<String> result = new ArrayList<>();
        for (TableContainer table : tableList) {
            result.add(table.getName());
        }
        return result;
    }

    /**
     * Returns -1 if table does not exist.
     * @param name
     * @return
     * @throws DevelopmentException
     * @throws TableManagerException
     */
    private int getTableIndex(String name) throws DevelopmentException, TableManagerException {
        int index = -1;
        String temp = tableIndexes.get(name.toString());
        if (temp == null) {
            return index;
        } else {
            try {
                index = Integer.parseInt(temp);
                return index;
            } catch (Exception e) {
                debuggingService.throwDevException(new DevelopmentException("Failed to get index for table " + name + " index is corrupt Class TableManagerService Method getTableIndex"));
                System.out.println("Failed to get index for table " + name + " index is corrupt.");
                throw new TableManagerException("Failed to get index for table " + name + " index is corrupt.");
            }
        }
    }
}