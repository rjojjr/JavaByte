package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.core.tables.TablePage;
import com.kirchnersolutions.utilities.ByteTools;
import com.kirchnersolutions.utilities.CryptTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HashPage implements Callable<byte[]> {

    private TablePage tablePage;

    public HashPage(TablePage tablePage){
        this.tablePage = tablePage;
    }

    @Override
    public byte[] call() throws Exception {
        Thread.currentThread().setName("Hash table page " + tablePage.getName());
        tablePage.setLoading(true);
        byte[] hash = new byte[1];
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
                    hash = CryptTools.getSHA256(new String(array, "UTF-8"));
                    first = false;
                }else{
                    hash = CryptTools.getSHA256(new String(hash, "UTF-8") + new String(array, "UTF-8"));
                }
            }
        }
        tablePage.setLoading(false);
        return hash;
    }
}
