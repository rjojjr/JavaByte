package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;
import com.kirchnersolutions.database.objects.Field;
import com.kirchnersolutions.utilities.ByteTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PageCSV implements Callable<byte[]> {

    private TablePage tablePage;
    private String delim;

    public PageCSV(TablePage tablePage, String delim){
        this.tablePage = tablePage;
        this.delim = delim;
    }

    @Override
    public byte[] call() throws Exception {
        tablePage.setLoading(true);
        File rowDir = tablePage.getRowDir();
        String csv = tablePage.getName() + "\n\r";
        boolean first = true;
        for (File row : rowDir.listFiles()){
            List<byte[]> bytes = new ArrayList<>();
            File[] fields = row.listFiles();
            for(File fieldFile : fields){
                bytes.add(ByteTools.readBytesFromFile(fieldFile));

            }
            for(byte[] array : bytes){
                if(first){
                    csv = csv + new String(array, "UTF-8");
                    first = false;
                }else{
                    csv = csv + delim + new String(array, "UTF-8");
                }
            }
            csv = csv + "\r\n";
            first = true;
        }
        tablePage.setLoading(false);
        return csv.getBytes("UTF-8");
    }
}
