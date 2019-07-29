package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.core.tables.TableContainer;
import com.kirchnersolutions.database.core.tables.TablePage;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@DependsOn({"configurationService", "devVars", "sysVars"})
@Service
@ApplicationScope
public class TableThreadService {

    @Autowired
    DevVars devVars;
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    SysVars sysVars;
    @Autowired
    DatabaseObjectFactory databaseObjectFactory;

    public AtomicBoolean createRows(List<TablePage> tables, Map<BigInteger, Map<String, String>> newRows) throws Exception{
        return CreateRows(tables, newRows);
    }

    public Map<BigInteger, Map<String, String>> deleteRows(List<BigInteger> indexes, List<TablePage> tables) throws Exception{
        return DeleteRows(indexes, tables);
    }

    public Map<BigInteger, Map<String, String>>  editFields(List<TablePage> tables, Map<BigInteger, Map<String, String>> changes) throws Exception{
        return EditFields(tables, changes);
    }

    public List<Map<String, String>> getRowsFromIndexList(List<TablePage> tablePages, List<BigInteger> indexes) throws Exception{
        return GetRowsFromIndexList(tablePages,indexes);
    }

    public Map<BigInteger, Map<String, String>> deleteTable(TablePage tablePage) throws Exception{
        return DeleteTable(tablePage);
    }

    public byte[] hashTable(TableContainer tableContainer) throws Exception{
        return HashTable(tableContainer);
    }

    private AtomicBoolean CreateRows(List<TablePage> tables, Map<BigInteger, Map<String, String>> newRows) throws Exception{
        Future<AtomicBoolean> future = threadPoolTaskExecutor.submit(new CreateRows(tables, newRows, threadPoolTaskExecutor, sysVars));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

    private Map<BigInteger, Map<String, String>> DeleteRows(List<BigInteger> indexes, List<TablePage> tables) throws Exception{
        Future<Map<BigInteger, Map<String, String>>> future = threadPoolTaskExecutor.submit(new DeleteRows(indexes, tables, threadPoolTaskExecutor));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

    private Map<BigInteger, Map<String, String>> EditFields(List<TablePage> tables, Map<BigInteger, Map<String, String>> changes) throws Exception{
        Future<Map<BigInteger, Map<String, String>>> future = threadPoolTaskExecutor.submit(new EditTableFields(tables, changes, threadPoolTaskExecutor));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

    private List<Map<String, String>> GetRowsFromIndexList(List<TablePage> tablePages, List<BigInteger> indexes) throws Exception{
        Future<List<Map<String, String>>> future = threadPoolTaskExecutor.submit(new GetRowsFromIndexList(tablePages, indexes, sysVars, databaseObjectFactory));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

    private Map<BigInteger, Map<String, String>> DeleteTable(TablePage tablePage) throws Exception{
        Future<Map<BigInteger, Map<String, String>>> future = threadPoolTaskExecutor.submit(new DeleteTable(tablePage));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

    private byte[] HashTable(TableContainer tableContainer) throws Exception{
        Future<byte[]> future = threadPoolTaskExecutor.submit(new HashTable(tableContainer));
        try{
            return future.get();
        }catch (Exception e){
            throw e;
        }
    }

}