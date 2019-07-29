package com.kirchnersolutions.database.core.tables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TableBench {

    @Autowired
    private TableManagerService tableManagerService;

    private long SortBench(String tableName) throws Exception{
        long stime = System.currentTimeMillis();
        Map<String, String> request = new HashMap<>();
        tableManagerService.searchTableAll(tableName, request);
        return System.currentTimeMillis() - stime;
    }

}
