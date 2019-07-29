package com.kirchnersolutions.database.core.tables.threads;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.core.tables.TablePage;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.Field;
import com.kirchnersolutions.utilities.ByteTools;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class GetRowsFromIndexList implements Callable<List<Map<String, String>>> {

    private SysVars sysVars;
    private DatabaseObjectFactory databaseObjectFactory;

    List<TablePage> tablePages = new ArrayList<>();
    List<BigInteger> indexes = null;

    public GetRowsFromIndexList(List<TablePage> tablePages, List<BigInteger> indexes, SysVars sysVars, DatabaseObjectFactory databaseObjectFactory){
        this.tablePages = tablePages;
        this.indexes = indexes;
        this.databaseObjectFactory = databaseObjectFactory;
        this.sysVars = sysVars;
    }

    @Override
    public List<Map<String, String>> call() throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        for(BigInteger index : indexes){
            Map<String, String> map = new HashMap<>();
            File rowDir = tablePages.get(tableIndexIsIn(index)).getRowDir();
            File row = new File(rowDir, sysVars.getFileSeperator() + index.toString());
            for(File fieldFile : row.listFiles()){
                Field field = (Field)databaseObjectFactory.databaseObjectFactory(ByteTools.readBytesFromFile(fieldFile));
                map.put(field.getName(), field.getValue());
            }
            rows.add(map);
        }
        return rows;
    }

    private int tableIndexIsIn(BigInteger index){
        BigInteger n = new BigInteger(tablePages.size() + "");
        BigInteger x = new BigInteger(index.toString());
        if(x.compareTo(n) == 1){
            int div = x.intValue() / n.intValue();
            BigInteger t1 = new BigInteger(div + "");
            BigInteger t2 = t1.multiply(n);
            BigInteger t4 = x.subtract(t2);
            if(t4.equals(new BigInteger("0"))){
                return n.intValue();
            }
            t4 = t4.subtract(new BigInteger("1"));
            return t4.intValue();
        }else{
            x = x.subtract(new BigInteger("1"));
            return x.intValue();
        }
    }
}