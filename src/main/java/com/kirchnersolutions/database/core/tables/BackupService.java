package com.kirchnersolutions.database.core.tables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class BackupService {

    @Autowired
    private TableManagerService tableManagerService;

    private File dir, temp, tablesDir;

    public BackupService(){
        dir = new File("/Database/Backup");
        temp = new File(dir, "/temp");
    }

}
