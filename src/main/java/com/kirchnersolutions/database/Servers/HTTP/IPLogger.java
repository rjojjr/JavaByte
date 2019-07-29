package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.utilities.ByteTools;
import com.kirchnersolutions.utilities.CalenderConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DependsOn("debuggingService")
@Component
public class IPLogger {

    @Autowired
    private DebuggingService debuggingService;

    private File dir;

    public IPLogger(){
        dir = new File("Database/logs/ip");
        if(!dir.exists()){
            dir.mkdirs();
        }
    }

    public void log(String ip, String page, String username) throws Exception{
        String time = CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "-", "~");
        File log = new File(dir, "/" + time + "_" + username + ".txt");
        log.createNewFile();
        String out = "IP: " + ip + "\r\nuser: " + username + "\r\nPage: " + page + "\r\nTime: " + time;
        debuggingService.ipDebug("Hit Logged: " + out.replace("\r\n", " - "));
        ByteTools.writeBytesToFile(log, out.getBytes("UTF-8"));
    }

    String getNumerLogs(){
        return dir.listFiles().length + "";
    }

    void dumpLogs(){
        for(File log : dir.listFiles()){
            log.delete();
        }
    }

    public List<String> getLogs(String ip, String page, String username, String startDate, String endDate){
        List<File> matches = new ArrayList<>();
        File[] logs = dir.listFiles();
        if(!startDate.equals("")){

            String uname = "";
            String[] temp = startDate.split("~");
            String[] temp1 = temp[0].split("-");
            long stime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                    , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));

            for(File log : logs){
                temp = log.getName().split("~");
                temp1 = temp[0].split("-");
                long ftime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                        , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3].split("_")[0]));

                if(!endDate.equals("")){
                    temp = endDate.split("~");
                    temp1 = temp[0].split("-");
                    long etime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                            , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
                    if(ftime >= stime && ftime <= etime){
                        matches.add(log);
                    }
                }else{
                    if(ftime >= stime){
                        matches.add(log);
                    }
                }
            }
            if(matches.isEmpty()){
                return null;
            }
            if(!ip.equals("")){
                matches = searchIPFromList(matches, ip);
                if(matches.isEmpty()){
                    return null;
                }
            }
            if(!username.equals("")){
                matches = searchUsernameFromList(matches, username);
                if(matches.isEmpty()){
                    return null;
                }
            }
            if(!page.equals("")){
                matches = searchPageFromList(matches, ip);
                if(matches.isEmpty()){
                    return null;
                }
            }
            return getStrings(matches);
        }else if(!endDate.equals("")){
            String[] temp = endDate.split("~");
            String[] temp1 = temp[0].split("-");
            long etime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                    , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
            for(File log : logs) {
                String time = log.getName();
                temp = log.getName().split("~");
                temp1 = temp[0].split("-");
                long ftime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                        , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3].split("_")[0]));
                if(ftime <= etime){
                    matches.add(log);
                }
            }
            if(matches.isEmpty()){
                return null;
            }
            if(!ip.equals("")){
                matches = searchIPFromList(matches, ip);
                if(matches.isEmpty()){
                    return null;
                }
            }
            if(!username.equals("")){
                matches = searchUsernameFromList(matches, username);
                if(matches.isEmpty()){
                    return null;
                }
            }
            if(!page.equals("")){
                matches = searchPageFromList(matches, ip);
                if(matches.isEmpty()){
                    return null;
                }
            }
            return getStrings(matches);
        }else if(!ip.equals("")){
            matches = searchLogs(ip, 0);
            if(matches.isEmpty()){
                return null;
            }
            if(!username.equals("")){
                matches = searchUsernameFromList(matches, username);
                if(matches.isEmpty()){
                    return null;
                }
            }
            if(!page.equals("")){
                matches = searchPageFromList(matches, page);
                if(matches.isEmpty()){
                    return null;
                }
            }
            return getStrings(matches);
        }else if (!username.equals("")){
            matches = searchLogs(username, 1);
            if(matches.isEmpty()){
                return null;
            }
            if(!page.equals("")){
                matches = searchPageFromList(matches, page);
                if(matches.isEmpty()){
                    return null;
                }
            }
            return getStrings(matches);
        }else if (!page.equals("")){
            matches = searchLogs(page, 2);
            if(matches.isEmpty()){
                return null;
            }
            return getStrings(matches);
        }else {
            matches = Arrays.asList(logs);
            return getStrings(matches);
        }
    }

    private List<String> getStrings(List<File> files){
        List<String> matches = new ArrayList<>();
        Collections.sort(files, new IPSorter());
        for(File file : files){
            try{
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                String line = "";
                boolean first = true;
                for(String string : temp){
                    if(first){
                        line = string;
                        first = false;
                    }else{
                        line+= ";" + string;
                    }
                }
                matches.add(line);
            }catch (Exception e){
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

    private List<File> searchUsernameFromList(List<File> files, String username){
        List<File> matches = new ArrayList<>();
        for(File file : files){
            try{
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                if(temp[1].split(": ")[1].equals(username)){
                    matches.add(file);
                }
            }catch (Exception e){
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

    private List<File> searchUsername(String username) {
        List<File> matches = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            try {
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                if (temp[1].split(": ")[1].equals(username)) {
                    matches.add(file);
                }
            } catch (Exception e) {
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

    private List<File> searchIPFromList(List<File> files, String ip){
        List<File> matches = new ArrayList<>();
        for(File file : files){
            try{
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                if(temp[0].split(": ")[1].equals(ip)){
                    matches.add(file);
                }
            }catch (Exception e){
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

    private List<File> searchLogs(String par, int ind){
        List<File> matches = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            try {
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                if (temp[ind].split(": ")[1].equals(par)) {
                    matches.add(file);
                }
            } catch (Exception e) {
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

    private List<File> searchPageFromList(List<File> files, String page){
        List<File> matches = new ArrayList<>();
        for(File file : files){
            try{
                byte[] bytes = ByteTools.readBytesFromFile(file);
                String contents = new String(bytes, "UTF-8");
                String[] temp = contents.split("\r\n");
                if(temp[2].split(": ")[1].equals(page)){
                    matches.add(file);
                }
            }catch (Exception e){
                debuggingService.nonFatalDebug("Failed to read log file " + file.getName());
            }
        }
        return matches;
    }

}
