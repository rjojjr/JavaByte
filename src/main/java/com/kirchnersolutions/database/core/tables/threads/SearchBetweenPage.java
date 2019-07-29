package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor()
class SearchBetweenPage implements Callable<List<Map<String, String>>> {

    private AtomicInteger found;
    private int howMany;
    private Map<String, String> request;
    private long start, end;
    private String fieldName;
    private TablePage tablePage;

    @Override
    public List<Map<String, String>> call() throws Exception {
        return tablePage.searchBetween(fieldName, start, end, request, howMany, found);
    }
}
