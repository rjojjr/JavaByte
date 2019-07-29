package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.objects.Field;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class TableMethods {


    static void compileRow(Map<String, String> row, File rowDir) throws Exception{
        List<Field> fieldList = new ArrayList<>();
        File[] fields = rowDir.listFiles();
        for (File fieldFile : fields) {
            Field field = new Field(fieldFile);
            row.put(field.getName(), field.getValue());
        }
    }

    static boolean verifyRow(Map<String, String> request, Map<String, String> row) {
        boolean afound = true;
        for (String key : row.keySet()) {
            String value = request.get(key);
            if (value != null) {
                if (row.get(key).equals(value)) {

                } else {
                    afound = false;
                    break;
                }
            }
        }
        return afound;
    }

}
