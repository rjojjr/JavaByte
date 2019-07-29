package com.kirchnersolutions.database.core.tables.threads;
/**
 *2019 Kirchner Solutions
 * @author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.core.tables.TablePage;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.TableThreadException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class SimpleSearchTablePage implements Callable<List<Map<String, String>>> {

    private DevVars devVars;

    private volatile TablePage tablePage = null;
    private Map<String, String> request;

    public SimpleSearchTablePage(TablePage tablePage, Map<String, String> request, DevVars devVars){
        this.tablePage = tablePage;
        this.request = request;
        this.devVars = devVars;
    }

    public List<Map<String, String>> call() throws DevelopmentException, TableThreadException{
        Thread.currentThread().setName(tablePage.getName().toString() + ":SimpleTablePageSearch:");
        List<Map<String, String>> result = null;
        try{
            result = tablePage.searchTablePageRows(request);
        }catch (Exception e){
            e.printStackTrace();
            if(devVars.isDevExceptions()){
                System.out.println("Exception in thread " + Thread.currentThread().getName() + " " + e.getMessage() + " Class SearchTablePage Method Call");
                throw new DevelopmentException("Exception in thread " + Thread.currentThread().getName() + " " + e.getMessage() + " Class SearchTablePage Method Call");
            }else{
                System.out.println("Exception in thread " + Thread.currentThread().getName() + " " + e.getMessage());
                throw new TableThreadException("Exception in thread " + Thread.currentThread().getName() + " " + e.getMessage());
            }
        }
        return result;
    }

}