package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.utilities.ByteTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@DependsOn({"debuggingService"})
@Service
public class BackupService {

    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private SysVars sysVars;

    public BackupService(){
        if(!new File("Database/Backup").exists()){
            File dir = new File("Database/Backup");
            dir.mkdirs();
        }
        startBackupService();
    }

    private void startBackupService() {
        try{
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File("JavaByteBackupService-" + sysVars.BACKUP_VERSION +".jar");

            /* is it a jar file? */
            if(!currentJar.getName().endsWith(".jar"))
                return;

            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());
            command.add("4434");
            final ProcessBuilder builder = new ProcessBuilder(command);
            System.out.println("Backup service is starting...");
            builder.start();
        }catch (Exception e){
            System.err.println("Unable to start backup service.");
            e.printStackTrace();
        }
        System.out.println("Backup service started.");
    }

    byte[] hashTable(String tableName) throws Exception{
        File dir = new File("Database/Backup/" + tableName);
        if(!dir.exists()){
            dir.mkdirs();
        }
        //File temp = new File(dir, "Database/temp");
        //File tablesDir = new File("Database/Tables");
        try{
            return tableManagerService.hashTableContainer(tableName);
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException("Failed to hash table " + tableName + " " + e.getMessage()));
            debuggingService.nonFatalDebug("Failed to hash table " + tableName + " " + e.getMessage());
        }
        return null;
    }

    public boolean writeTableHash(String tableName) throws Exception{
        File dir = new File("Database/Backup/" + tableName);
        File hash = new File(dir + "/" + tableName + ".hash");
        if(!hash.exists()){
            hash.createNewFile();
        }
        ByteTools.writeBytesToFile(hash, hashTable(tableName));
        return true;
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}
