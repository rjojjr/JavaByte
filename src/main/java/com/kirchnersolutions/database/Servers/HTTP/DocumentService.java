package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.utilities.ByteTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Scanner;

@DependsOn("debuggingService")
@Service
public class DocumentService {

    @Autowired
    private DebuggingService debuggingService;

    private final File changeLog = new File("Database//Doc//JavaByteChangelog.txt");
    private final File doc = new File("Database//Doc//JavaByte_Documentation.txt");

    String getChangelog() throws Exception{
        String out = "";
        boolean first = true;
        try{
            out = new String(ByteTools.readBytesFromFile(changeLog), "UTF-8");
            return out;
        }catch (Exception e){
            e.printStackTrace();
            debuggingService.throwDevException(new DevelopmentException("Failed to read changelog at Class DocumentService Method getChangelog"));
            debuggingService.nonFatalDebug(e.getMessage());
        }
        return out;
    }

    String getDocs() throws Exception{
        String out = "";
        boolean first = true;
        Scanner in = null;
        try{
            in = new Scanner(doc);
            while(in.hasNextLine()){
                String line = in.nextLine();
                if(first){
                    out = line;
                    first = false;
                }else{
                    out+= "\n" + line;
                }
            }
            return out;
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException("Failed to read documentation at Class DocumentService Method getDocs"));
            debuggingService.nonFatalDebug(e.getMessage());
        }finally {
            in.close();
        }
        return out;
    }

}
