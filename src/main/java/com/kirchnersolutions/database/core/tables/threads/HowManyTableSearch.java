package com.kirchnersolutions.database.core.tables.threads;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.core.tables.TablePage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class HowManyTableSearch implements Callable<List<Map<String, String>>> {

    private AtomicInteger found;
    private int howMany;
    private TablePage tablePage;
    private Map<String, String> request;

    public HowManyTableSearch(AtomicInteger found, int howMany, TablePage tablePage, Map<String, String> request) {
        this.found = found;
        this.howMany = howMany;
        this.tablePage = tablePage;
        this.request = request;
    }

    public List<Map<String, String>> call() throws Exception{
        Thread.currentThread().setName(tablePage.getName().toString() + ":HowManyTableSearch:");
        List<Map<String, String>> result = null;
        try {
            result = tablePage.searchTablePageRowsHowMany(request, found, howMany);
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

}