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
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.*;

import com.kirchnersolutions.database.objects.Field;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.utilities.ByteTools;
import com.kirchnersolutions.utilities.SerialService.GeneralSerializer;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class TablePage {

    private DevVars devVars;
    private DebuggingService debuggingService;
    private SysVars sysVars;
    private GeneralSerializer generalSerializer;
    private DatabaseObjectFactory databaseObjectFactory;

    private TableContainer tableContainer;

    private String name;
    private File tableDir, rowDir, def, fieldIndexes;
    private String[] fields, indexedFields;
    private volatile List<Map<String, List<String>>> indexList;
    private volatile AtomicBoolean loading = new AtomicBoolean(false);
    private volatile AtomicInteger rowCountInt = new AtomicInteger(0);

    /**
     * Initializes an existing TablePage, or creates a new one if it doesn't exist.
     *
     * @param tableDir
     * @param tableContainer
     * @throws DevelopmentException
     * @throws TableException
     */
    public TablePage(File tableDir, TableContainer tableContainer) throws DevelopmentException, TableException {
        this.generalSerializer = tableContainer.generalSerializer;
        this.devVars = tableContainer.devVars;
        this.sysVars = tableContainer.sysVars;
        this.databaseObjectFactory = tableContainer.databaseObjectFactory;
        this.tableDir = tableDir;
        this.tableContainer = tableContainer;
        this.debuggingService = tableContainer.debuggingService;
        indexList = Collections.synchronizedList(new ArrayList<>());
        if (!tableDir.exists()) {
            tableDir.mkdirs();
            name = tableDir.getName();
            File tDir = tableContainer.getTableDir();
            def = new File(tDir, sysVars.getFileSeperator() + "definition");
            fieldIndexes = new File(tDir, sysVars.getFileSeperator() + "index");
            if (!def.exists()) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist Class TablePage Method Constructor(File)");
                    throw new DevelopmentException("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist Class TablePage Method Constructor(File)");
                } else {
                    System.out.println("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist");
                    throw new TableException("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist");
                }
            } else {
                try {
                    fields = (String[]) generalSerializer.deserialize(ByteTools.readBytesFromFile(def));
                } catch (Exception e) {
                    if (devVars.isDevExceptions()) {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                        throw new DevelopmentException("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                    } else {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage());
                        throw new TableException("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage());
                    }
                }
            }
            if (!fieldIndexes.exists()) {
                indexedFields = null;
            } else {
                try {
                    indexedFields = (String[]) generalSerializer.deserialize(ByteTools.readBytesFromFile(fieldIndexes));
                } catch (Exception e) {
                    if (devVars.isDevExceptions()) {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                        throw new DevelopmentException("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                    } else {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage());
                        throw new TableException("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage());
                    }
                }
            }
        } else {
            name = tableDir.getName();
            File tDir = tableContainer.getTableDir();
            def = new File(tDir, sysVars.getFileSeperator() + "definition");
            fieldIndexes = new File(tDir, sysVars.getFileSeperator() + "index");
            if (!def.exists()) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist Class TablePage Method Constructor(File)");
                    throw new DevelopmentException("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist Class TablePage Method Constructor(File)");
                } else {
                    System.out.println("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist");
                    throw new TableException("Failed to initialize table " + tDir.getParent() + " definition. Definition file doesn't exist");
                }
            } else {
                try {
                    fields = (String[]) generalSerializer.deserialize(ByteTools.readBytesFromFile(def));
                } catch (Exception e) {
                    if (devVars.isDevExceptions()) {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                        throw new DevelopmentException("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage() + " Class TablePage Method Constructor(File)");
                    } else {
                        System.out.println("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage());
                        throw new TableException("Failed to initialize table " + tDir.getParent() + " definition. " + e.getMessage());
                    }
                }
            }
            if (!fieldIndexes.exists()) {
                indexedFields = null;
            } else {
                try {
                    byte[] bytes = ByteTools.readBytesFromFile(fieldIndexes);
                    if (bytes[0] == -1) {
                        indexedFields = null;
                    } else {
                        indexedFields = (String[]) generalSerializer.deserialize(bytes);
                    }

                } catch (Exception e) {
                    debuggingService.throwDevException(new DevelopmentException("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage() + " Class TablePage Method Constructor(File)"));
                    debuggingService.nonFatalDebug("Failed to initialize table " + tDir.getParent() + " indexed fields. " + e.getMessage());
                }
            }

            rowDir = new File(tableDir, "/Rows");
            if (!rowDir.exists()) {
                rowDir.mkdirs();
            }
        }
    }

    public String getName() {
        return new String(name);
    }

    /**
     * Returns table page loading status.
     *
     * @return
     */
    public boolean isLoading() {
        return loading.get();
    }

    /**
     * Loads indexes.
     *
     * @throws DevelopmentException
     * @throws TableException
     */
    public void load() throws DevelopmentException, TableException {
        loadToMemory();
    }

    public void setLoading(boolean value) {
        loading.set(value);
    }

    /**
     * Returns number of rows in table page.
     *
     * @return
     */
    public BigInteger getRowCount() {
        //System.out.println(RowCount());
        return new BigInteger(RowCount() + "");
    }

    /**
     * Creates row in table page with the give index and fields.
     *
     * @param index
     * @param fields
     * @return
     * @throws Exception
     */
    public boolean createRow(BigInteger index, List<Field> fields) throws Exception {
        return CreateRow(index, fields);
    }

    /**
     * Returns all rows matching request.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> searchTablePageRows(Map<String, String> request) throws Exception {
        List<String> indexes = searchTable(request);
        return generateRowList(indexes);
    }

    /**
     * Returns howMany rows mating request.
     *
     * @param request
     * @param found
     * @param howMany
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> searchTablePageRowsHowMany(Map<String, String> request, AtomicInteger found, int howMany) throws Exception {
        List<String> indexes = searchTableHowMany(request, howMany, found);
        return generateRowList(indexes);
    }

    /**
     * Edits fields of rows with indexes BigInteger.
     * Returns null if operation failed and rollback is performed.
     *
     * @param changes
     * @param needRollback
     * @return
     * @throws Exception
     */
    public Map<BigInteger, Map<String, String>> editFields(Map<BigInteger, Map<String, String>> changes, AtomicBoolean needRollback) throws Exception {
        return editField(changes, needRollback);
    }

    /**
     * Rolls back all fields included in oldValues.
     *
     * @param oldValues
     * @throws Exception
     */
    public void rollback(Map<BigInteger, Map<String, String>> oldValues) throws Exception {
        try {
            Rollback(oldValues);
        } catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException("Rollback error in table page " + tableDir.getName() + " Exception: " + e.getMessage() + " Class TablePage Method rollback"));
            debuggingService.nonFatalDebug("Rollback error in table page " + tableDir.getName() + " Exception: " + e.getMessage());
        }
    }

    /**
     * Deletes entire table page.
     */
    public Map<BigInteger, Map<String, String>> deleteTablePage() throws Exception {
        return DeleteTablePage();
    }

    /**
     * @param index
     * @param fieldMap
     * @return
     * @throws Exception
     */
    public boolean createRow(BigInteger index, Map<String, String> fieldMap, AtomicBoolean needRollback) throws Exception {
        return CreateRow(index, fieldMap, needRollback);
    }

    /**
     * Deletes the row with the given index and returns its old values for rollback purposes.
     * Returns null if row does not exist.
     *
     * @param index
     * @return
     * @throws Exception
     */
    public Map<String, String> deleteRow(BigInteger index) throws Exception {
        return DeleteRow(index);
    }

    public List<Map<String, String>> searchBetween(String fieldName, long start, long end, Map<String, String> request, int howMany, AtomicInteger found) throws Exception {
        return generateRowList(SearchCompoundBetween(fieldName, start, end, request, howMany, found));
    }

    /**
     * Returns the row diurectory for current table page.
     *
     * @return
     */
    public File getRowDir() {
        return this.rowDir;
    }

    public void removeRowsFromMaps(BigInteger index) throws Exception {
        removeFromMaps(index);
    }

    private Map<String, String> DeleteRow(BigInteger index) throws Exception {
        Map<BigInteger, Map<String, String>> oldValues = new HashMap<>();
        Map<String, String> fieldMap = new HashMap<>();
        File row = new File(rowDir, sysVars.getFileSeperator() + index.toString());
        if (row.exists()) {
            if (indexedFields != null) {
                removeFromMaps(index);
            }
            for (File field : row.listFiles()) {
                Field old = new Field(field);
                fieldMap.put(old.getName(), old.getValue());
                old = null;
                field.delete();
            }
            row.delete();
            return fieldMap;
        } else {
            return null;
        }

    }

    private int RowCount() {
        if (indexedFields == null) {
            return rowDir.listFiles().length;
        } else {
            return rowCountInt.intValue();
        }
    }

    private synchronized void increaseRowCount() {
        rowCountInt.addAndGet(1);
    }

    private Map<BigInteger, Map<String, String>> editField(Map<BigInteger, Map<String, String>> changes, AtomicBoolean needRollback) throws Exception {
        List<BigInteger> indexes = new ArrayList<>(changes.keySet());
        List<File> rowFiles = new ArrayList<>();
        Map<BigInteger, Map<String, String>> oldValues = new HashMap<>();
        for (BigInteger index : indexes) {
            //System.out.println(index);
            if (needRollback.get()) {
                break;
            }
            File temp = new File(rowDir, sysVars.getFileSeperator() + index.toString());
            if (temp.exists()) {
                rowFiles.add(temp);
                Map<String, String> change = changes.get(index);
                Map<String, String> old = new HashMap<>();
                List<Field> fields = getFields(temp.listFiles());
                for (Field field : fields) {
                    if (change.containsKey(field.getName())) {
                        old.put(field.getName(), field.getValue());
                    }
                }
                oldValues.put(index, old);
            }
        }
        for (File rowFile : rowFiles) {
            if (needRollback.get()) {
                break;
            }
            if (!changes.isEmpty()) {
                BigInteger rowIndex = new BigInteger(rowFile.getName());
                Map<String, String> change = changes.get(rowIndex);
                for (String key : change.keySet()) {
                }
                try {
                    List<Field> fields = getFields(rowFile.listFiles());
                    for (Field field : fields) {
                        if (change.containsKey(field.getName())) {
                            field.setValue(change.get(field.getName()));
                            field.write();
                        }
                    }
                    changes.remove(rowIndex);
                } catch (Exception e) {
                    needRollback.set(true);
                    Rollback(oldValues);
                    debuggingService.throwDevException(new DevelopmentException("Failed to edit field in table page " + tableDir.getName() + "Exception " + e.getMessage() + " rollback required. Class TablePage Method editFields"));
                    debuggingService.nonFatalDebug("Failed to edit field in table page " + tableDir.getName() + "Exception " + e.getMessage() + " rollback required.");
                }
            }
        }
        if (needRollback.get()) {
            Rollback(oldValues);
            return null;
        }
        return oldValues;
    }

    private boolean CreateRow(BigInteger index, Map<String, String> fieldMap, AtomicBoolean needRollback) throws Exception {
        if (fieldMap.size() != fields.length) {
            debuggingService.throwDevException(new DevelopmentException("Failed to create row " + index.toString() + " in table page " + tableDir.getName() + " wrong number of field arguments. Class TablePage Method CreateRow"));
            debuggingService.nonFatalDebug("Failed to create row " + index.toString() + " in table page " + tableDir.getName() + " wrong number of field arguments.");
            return false;
        }
        File rowFolder = new File(rowDir, sysVars.getFileSeperator() + index.toString());
        rowFolder.mkdirs();
        if (!needRollback.get()) {
            for (String fieldName : fields) {
                File file = new File(rowFolder, sysVars.getFileSeperator() + fieldName.split("-")[0]);
                String value = fieldMap.get(new String(fieldName.split("-")[0]));
                if (value == null) {
                    needRollback.set(true);
                    debuggingService.throwDevException(new DevelopmentException("Failed to create row " + index.toString() + " in table page " + tableDir.getName() + " invalid field arguments. Class TablePage Method CreateRow"));
                    debuggingService.nonFatalDebug("Failed to create row " + index.toString() + " in table page " + tableDir.getName() + " invalid field arguments.");
                    return false;
                } else {
                    //Field newField = new Field(file, new String(fieldName.split("-")[0]), value, fieldName.split("-")[1]);
                    Field newField = new Field(file, new String(fieldName.split("-")[0]), value, "s");
                    newField.write();
                }
            }
            if (indexedFields != null) {
                addToMaps(rowFolder);
            }
            return true;
        }
        return false;
    }

    private void Rollback(Map<BigInteger, Map<String, String>> oldValues) throws Exception {
        List<BigInteger> indexes = new ArrayList<>(oldValues.keySet());
        for (BigInteger index : indexes) {
            File temp = new File(rowDir, sysVars.getFileSeperator() + index.toString());
            if (temp.exists()) {
                Map<String, String> change = oldValues.get(index);
                List<Field> fields = getFields(temp.listFiles());
                for (Field field : fields) {
                    if (change.containsKey(field.getName())) {
                        field.setValue(change.get(field.getName()));
                    }
                }
                oldValues.remove(index);
            }
        }
    }

    private int getFieldIndex(String fieldName) throws TableException, DevelopmentException {
        for (int i = 0; i < fields.length; i++) {
            if (fieldName.equals(fields[1])) {
                return i;
            }
        }
        if (devVars.isDevExceptions()) {
            System.out.println("Field " + fieldName + " does not exist in table page " + tableDir.getName() + " Class TablePage Method getFieldIndex");
            throw new DevelopmentException("Field " + fieldName + " does not exist in table page " + tableDir.getName() + " Class TablePage Method getFieldIndex");
        } else {
            System.out.println("Field " + fieldName + " does not exist in table page " + tableDir.getName());
            throw new TableException("Field " + fieldName + " does not exist in table page " + tableDir.getName());
        }
    }

    private void createMaps() {
        if (indexedFields != null) {
            for (String field : indexedFields) {
                indexList.add(new ConcurrentHashMap<>());
            }
        }
    }

    private List<Field> getFields(File[] fieldFiles) throws DevelopmentException, TableException {
        List<Field> fields = new ArrayList<>();
        for (File field : fieldFiles) {
            try {
                Field newField = new Field(field);
                //newField = new Field(field);
                fields.add(newField);
            } catch (Exception e) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Field " + field.getName() + " does not exist in row " + field.getParent() + " " + e.getMessage() + " Class TablePage Method getFields");
                    throw new DevelopmentException("Field " + field.getName() + " does not exist in row " + field.getParent() + " " + e.getMessage() + " Class TablePage Method getFields");
                } else {
                    System.out.println("Field " + field.getName() + " does not exist in row " + field.getParent() + " " + e.getMessage());
                    throw new TableException("Field " + field.getName() + " does not exist in row " + field.getParent() + " " + e.getMessage());
                }
            }
        }
        return fields;
    }

    private boolean CreateRow(BigInteger index, List<Field> fields) throws Exception {
        File rowFile = new File(rowDir, "/" + index.toString());
        if (rowFile.exists()) {
            if (devVars.isDevExceptions()) {
                System.out.println("Row " + index.toString() + " Already exist in table page " + tableDir.getName() + " Class TablePage Method CreateRow");
                throw new DevelopmentException("Row " + index.toString() + " Already exist in table page " + tableDir.getName() + " Class TablePage Method CreateRow");
            } else {
                System.out.println("Row " + index.toString() + " Already exist in table page " + tableDir.getName());
                throw new TableException("Row " + index.toString() + " Already exist in table page " + tableDir.getName());
            }
        } else {
            if (fields.size() != this.fields.length - 1) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Cannot create row " + index.toString() + ". Row contains the wrong number of fields" + " Class TablePage Method CreateRow");
                    throw new DevelopmentException("Cannot create row " + index.toString() + ". Row contains the wrong number of fields" + " Class TablePage Method CreateRow");
                } else {
                    System.out.println("Cannot create row " + index.toString() + ". Row contains the wrong number of fields");
                    throw new TableException("Cannot create row " + index.toString() + ". Row contains the wrong number of fields");
                }
            } else {
                rowFile.mkdirs();
                Field indField = new Field(new File(rowFile, sysVars.getFileSeperator() + "index"), new String("index"), new String(index.toString()), "type");
                for (Field field : fields) {
                    field.setFile(new File(rowFile, sysVars.getFileSeperator() + field.getName().toString()));
                }
                increaseRowCount();
                if (indexedFields != null) {
                    addToMaps(rowFile);
                }
                return true;
            }
        }
    }

    private List<Map<String, String>> generateRowList(List<String> indexes) throws Exception {
        File[] rowFiles = new File[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            rowFiles[i] = new File(rowDir, sysVars.getFileSeperator() + indexes.get(i).toString());
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (File rowFile : rowFiles) {
            Map<String, String> row = new HashMap<>();
            row.put("index", rowFile.getName());
            List<Field> fieldList = new ArrayList<>();
            File[] fields = rowFile.listFiles();
            for (File fieldFile : fields) {
                Field field = new Field(fieldFile);
                row.put(field.getName(), field.getValue());
            }
            rows.add(row);
        }
        return rows;
    }

    private List<String> searchRowList(List<String> prevResults, Map<String, String> request) throws Exception {
        File[] rowFiles = new File[prevResults.size()];
        for (int i = 0; i < prevResults.size(); i++) {
            rowFiles[i] = new File(rowDir, sysVars.getFileSeperator() + prevResults.get(i));
        }
        prevResults = new ArrayList<>();
        for (File rowFile : rowFiles) {
            Map<String, String> row = new HashMap<>();
            TableMethods.compileRow(row, rowFile);
            if (TableMethods.verifyRow(request, row)) {
                prevResults.add(row.get("index"));
            }
        }
        return prevResults;
    }

    private List<String> searchListHowMany(List<String> prevResults, Map<String, String> request, int howMany, AtomicInteger found) throws Exception {
        File[] rowFiles = new File[prevResults.size()];
        for (int i = 0; i < prevResults.size(); i++) {
            rowFiles[i] = new File(rowDir, sysVars.getFileSeperator() + prevResults.get(i));
        }
        prevResults = new ArrayList<>();
        for (File rowFile : rowFiles) {
            if (found.get() == howMany && howMany > 0) {
                break;
            }
            Map<String, String> row = new HashMap<>();
            TableMethods.compileRow(row, rowFile);
            if (TableMethods.verifyRow(request, row)) {
                found.addAndGet(1);
                prevResults.add(row.get(new String("index")));
            }
        }
        return prevResults;
    }

    private Map<BigInteger, Map<String, String>> DeleteTablePage() throws Exception {
        Map<BigInteger, Map<String, String>> oldValues = new HashMap<>();
        if (indexedFields != null) {
            for (Map<String, List<String>> index : indexList) {
                index = null;
            }
            indexList = null;
        }
        File[] rows = rowDir.listFiles();
        for (File row : rows) {
            File[] fields = row.listFiles();
            Map<String, String> rowMap = new HashMap<>();
            for (File field : fields) {
                try {
                    Field rowField = new Field(field);
                    rowMap.put(rowField.getName(), rowField.getValue());
                } catch (Exception e) {
                    if (devVars.isDevExceptions()) {
                        System.out.println("Cannot delete table page " + tableDir.getName() + ". Data is corrupt" + " Class TablePage Method DeleteTablePage");
                        throw new DevelopmentException("Cannot delete table page " + tableDir.getName() + ". Data is corrupt" + " Class TablePage Method DeleteTablePage");
                    } else {
                        System.out.println("Cannot delete table page " + tableDir.getName() + ". Data is corrupt");
                        throw new TableException("Cannot delete table page " + tableDir.getName() + ". Data is corrupt");
                    }
                }
                field.delete();
            }
            oldValues.put(new BigInteger(rowMap.get("index").toString()), rowMap);
        }
        rowDir.delete();
        tableDir.delete();
        return oldValues;
    }

    private List<String> searchTableHowMany(Map<String, String> request, int howMany, AtomicInteger found) throws Exception {
        List<String> results;
        if (indexedFields != null) {
            results = searchMaps(request);
        } else {
            results = null;
        }
        if (results != null) {
            return searchRowList(results, request);
        } else {
            results = new ArrayList<>();
            File[] rowFiles = rowDir.listFiles();
            for (int i = rowFiles.length - 1; i >= 0; i--) {
                File rowFile = rowFiles[i];

                if (found.get() == howMany && howMany > 0) {
                    break;
                }
                Map<String, String> row = new HashMap<>();
                row.put("index", rowFile.getName());
                TableMethods.compileRow(row, rowFile);
                if (TableMethods.verifyRow(request, row)) {
                    found.addAndGet(1);
                    results.add(row.get(new String("index")));
                }
            }
        }
        return results;
    }


    private List<String> searchTable(Map<String, String> request) throws Exception {
        List<String> results = new ArrayList<>();
        if (indexedFields != null) {
            results = searchMaps(request);
            if (results != null) {
                return searchRowList(results, request);
            }
        } else {
            results = new ArrayList<>();
            File[] rowFiles = rowDir.listFiles();
            //for (File rowFile : rowFiles) {
            for (int i = rowFiles.length - 1; i >= 0; i--) {
                File rowFile = rowFiles[i];
                Map<String, String> row = new HashMap<>();
                row.put("index", rowFile.getName());
                TableMethods.compileRow(row, rowFile);
                if (TableMethods.verifyRow(request, row)) {
                    results.add(row.get(new String("index")));
                }
            }
        }
        return results;
    }

    /*
    key = fieldName, value = fieldValue
     */
    private List<String> searchMaps(Map<String, String> request) {
        if (indexedFields == null) {
            return null;
        }
        List<String> results = new ArrayList<>();
        for (String fieldName : request.keySet()) {
            int count = 0, mapIndex = 0;
            for (String fName : indexedFields) {
                if (fName.equals(fieldName.toString())) {
                    Map<String, List<String>> map = indexList.get(count);
                    List<String> inds = map.get(request.get(fieldName));
                    if (inds != null) {
                        request.remove((fieldName));
                        return inds;
                        //results = CSVTool.consolidateList(results, inds);
                    }
                    count++;
                }
                mapIndex++;
            }
        }
        return null;
    }

    /*
    private void deleteRow(BigInteger index) throws DevelopementException, TableException, FieldException, IllegalOperationException {
        File r = new File(rowDir, sysVars.getFileSeperator() + index.toString());
        if (r.exists()) {
            removeFromMaps(index);
            for (File rowf : r.listFiles()) {
                rowf.delete();
            }
            r.delete();
            rowCountInt.addAndGet(-1);
        } else {
            if (devVars.isDevExceptions()) {
                System.out.println("Cannot delete row " + index.toString() + ". Row does not exist. Class TablePage Method deleteRow");
                throw new DevelopementException("Cannot delete row " + index.toString() + ". Row does not exist. Class TablePage Method deleteRow");
            } else {
                System.out.println("Cannot delete row " + index.toString() + ". Row does not exist.");
                throw new TableException("Cannot delete row " + index.toString() + ". Row does not exist.");
            }
        }
    }*/

    private boolean removeFromMaps(BigInteger index) throws DevelopmentException, TableException, FieldException, IllegalOperationException {
        if (indexedFields != null) {
            File row = new File(rowDir, sysVars.getFileSeperator() + index.toString());
            if (!row.exists()) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Cannot remove row " + index.toString() + " from index. Row does not exist. Class TablePage Method removeFromMap");
                    throw new DevelopmentException("Cannot remove row " + index.toString() + " from index. Row does not exist. Class TablePage Method removeFromMap");
                } else {
                    System.out.println("Cannot remove row " + index.toString() + " from index. Row does not exist.");
                    throw new TableException("Cannot remove row " + index.toString() + " from index. Row does not exist.");
                }
            } else {
                int count = 0;
                for (File file : row.listFiles()) {
                    for (String iField : indexedFields) {
                        if (file.getName().equals(iField)) {
                            try {
                                Field ind = (Field) databaseObjectFactory.databaseObjectFactory(ByteTools.readBytesFromFile(file));
                                ind.setFile(file);
                                Map<String, List<String>> map = indexList.get(count);
                                List<String> indexes = map.get(ind.getValue());
                                indexes.remove(new String(index.toString()));
                                count++;
                                break;
                            } catch (Exception e) {
                                if (devVars.isDevExceptions()) {
                                    System.out.println("Failure to read row " + index.toString() + " field " + file.getName() + " Class TablePage Method removeFromMap");
                                    throw new DevelopmentException("Failure to read row " + index.toString() + " field " + file.getName() + " Class TablePage Method removeFromMap");
                                } else {
                                    System.out.println("Failure to read row " + index.toString() + " field " + file.getName());
                                    throw new TableException("Failure to read row " + index.toString() + " field " + file.getName());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return true;
    }

    private List<String> SearchCompoundBetween(String fieldName, long start, long end, Map<String, String> request, int howMany, AtomicInteger found) throws Exception {
        List<String> intResult = SearchBetween(fieldName, start, end);
        if (request == null) {
            return intResult;
        }
        return searchListHowMany(intResult, request, howMany, found);
    }

    private List<String> SearchBetween(String fieldName, long start, long end) throws Exception {
        List<String> results = new ArrayList<>();
        File[] rowFiles = rowDir.listFiles();
        //for (File rowFile : rowFiles) {
        for (int i = rowFiles.length - 1; i >= 0; i--) {
            File rowFile = rowFiles[i];
            File target = new File(rowFile, "/" + fieldName);
            Field targetField = new Field(target);
            try {
                long temp = Long.parseLong(targetField.getValue());
                if (temp >= start && temp <= end) {
                    results.add(rowFile.getName());
                }
            } catch (Exception e) {

            }
        }
        return results;
    }


    private boolean addToMaps(File row) throws DevelopmentException, TableException {
        if (indexedFields != null) {
            File[] fieldArray = new File[indexedFields.length];
            int count = 0;
            File[] fields = row.listFiles();
            File[] indexf = new File[1];
            Field index = null;
            for (File field : fields) {
                for (String fieldName : indexedFields) {
                    if (field.getName().equals("index")) {
                        indexf[0] = field;
                        index = getFields(indexf).get(0);
                    }
                    if (field.getName().equals(fieldName)) {
                        fieldArray[count] = field;
                        count++;
                    }
                    if (count == indexedFields.length) {
                        break;
                    }
                }
            }
            if (index != null) {
                List<Field> list = getFields(fieldArray);
                count = 0;
                for (Field field : list) {
                    Map<String, List<String>> map = indexList.get(count);
                    List<String> indexes = map.get(field.getValue());
                    if (indexes == null) {
                        indexes = new ArrayList<String>();
                        indexes.add(index.getValue());
                        map.put(field.getValue(), indexes);
                    } else {
                        indexes.add(index.getValue());
                        map.put(field.getValue(), indexes);
                    }
                    count++;
                }
                return true;
            } else {
                if (devVars.isDevExceptions()) {
                    System.out.println("Row " + row.getName() + " has corrupt index. Class TablePage Method loadToMemeory");
                    throw new DevelopmentException("Row " + row.getName() + " has corrupt index. Class TablePage Method loadToMemory");
                } else {
                    System.out.println("Row " + row.getName() + " has corrupt index.");
                    throw new TableException("Row " + row.getName() + " has corrupt index.");
                }
            }
        }
        return true;
    }

    private void loadToMemory() throws DevelopmentException, TableException {
        loading.set(true);
        createMaps();
        File[] rows = rowDir.listFiles();
        rowCountInt = new AtomicInteger(rows.length);
        if (indexedFields != null) {
            for (File row : rows) {
                addToMaps(row);
            }
            rows = null;
            loading.set(false);
        } else {
            rows = null;
            loading.set(false);
        }
    }

}