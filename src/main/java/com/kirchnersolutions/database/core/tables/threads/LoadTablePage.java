package com.kirchnersolutions.database.core.tables.threads;
/**
 *2019 Kirchner Solutions
 * @Author Robert Kirchner Jr.
 *
 * This code may not be decompiled, recompiled, copied, redistributed or modified
 * in any way unless given express written consent from Kirchner Solutions.
 */
import com.kirchnersolutions.database.core.tables.TablePage;

public class LoadTablePage implements Runnable {

    private TablePage tablePage;

    public LoadTablePage(TablePage tablePage){
        this.tablePage = tablePage;
    }

    public void run() {
        Thread.currentThread().setName(tablePage.getName().toString() + ":LoadTablePage:");
        try{
            tablePage.load();
        }catch (Exception e){
            try{
                throw e;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}