package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
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
        Scanner in = null;
        try{
            in = new Scanner(changeLog);
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
            debuggingService.throwDevException(new DevelopmentException("Failed to read changelog at Class DocumentService Method getChangelog"));
        }finally {
            in.close();
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
        }finally {
            in.close();
        }
        return out;
    }

}
