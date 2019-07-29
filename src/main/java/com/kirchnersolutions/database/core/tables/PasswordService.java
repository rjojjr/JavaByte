package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.Configuration.SysVars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@DependsOn("tableManagerService")
public class PasswordService {

    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private SysVars sysVars;

    private volatile boolean passwordsExpire;
    private volatile long passwordLife;

    public PasswordService() throws Exception {

    }

    @PostConstruct
    public void init() throws Exception{
        File dir = new File(tableManagerService.getTablesDir(), "/PasswordLogs");
        if (sysVars.getPasswordLife().equals("none")) {
            passwordsExpire = false;
            passwordLife = 0;
        } else {
            passwordsExpire = true;
            long days = Long.parseLong(sysVars.getPasswordLife());
            passwordLife = days * 24 * 60 * 60;
        }
        if (!dir.exists()) {
            String[] fields = new String[2];
            fields[0] = "userindex-i";
            fields[1] = "passwordtime-i";
            tableManagerService.createNewTable(new String("PasswordLogs"), fields, null);
        }
    }

    /**
     * Returns false if password is expired.
     *
     * @param userindex
     * @return
     * @throws Exception
     */
    boolean checkPassword(String userindex) throws Exception {
        return CheckPassword(userindex);
    }

    boolean updatePassword(String userindex) throws Exception {
        return UpdatePassword(userindex);
    }

    private boolean CheckPassword(String userindex) throws Exception {
        if (!passwordsExpire) {
            return true;
        } else {
            Map<String, String> request = new HashMap<>();
            request.put(new String("userindex"), userindex);
            List<Map<String, String>> results = tableManagerService.searchTable(new String("PasswordLogs"), request, 1);
            request = results.get(0);
            if (request != null) {
                long created = Long.parseLong(request.get(new String("passwordtime-i")).toString());
                if ((created + passwordLife) >= System.currentTimeMillis()) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean UpdatePassword(String userindex) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put(new String("userindex"), userindex);
        //
        List<Map<String, String>> results = tableManagerService.searchTable(new String("PasswordLogs"), request, 1);
        Map<BigInteger, Map<String, String>> editRows = new HashMap<>();
        if (editRows == null || editRows.isEmpty()) {
            request.put(new String("userindex"), userindex);
            request.put(new String("passwordtime"), new String(System.currentTimeMillis() + ""));
            results = new ArrayList<>();
            results.add(request);
            tableManagerService.createRows(results, new String("PasswordLogs"));
            return true;
        }
        request.put(new String("passwordtime"), new String(System.currentTimeMillis() + ""));
        String index = results.get(0).get(new String("index"));
        editRows.put(new BigInteger(index.toString()), request);
        try {
            tableManagerService.editRows(editRows, new String("PasswordLogs"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}