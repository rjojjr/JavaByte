package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionRepository;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;

@Service
public class MaintenanceService {

    @Autowired
    private SysVars sysVars;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private DebuggingService debuggingService;

    public void restartApplication(Session admin){
        RestartApplication(admin);
    }

    public void shutdown(Session admin){
        Shutdown(admin);
    }

    boolean reloadAllTables() throws Exception{
        return ReloadAllTables();
    }

    private void RestartApplication(Session admin) {

        try{
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File("JavaByte-" + sysVars.VERSION +".jar");

            /* is it a jar file? */
            if(!currentJar.getName().endsWith(".jar"))
                return;

            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            System.out.println("Server is restarting...");
            System.out.println("Server is shutting down...");
            endSessions(admin);
            builder.start();
            System.exit(0);
        }catch (Exception e){
            System.err.println("Unable to restart server");
            e.printStackTrace();
        }
    }

    private void Shutdown(Session admin){
        System.out.println("Server is shutting down...");
        endSessions(admin);
        System.exit(0);
    }

    private boolean endSessions(Session admin){
        System.out.println("Invalidating all active sessions...");
        try {
            if(sessionService.shutdown(admin)){
                System.out.println("All active sessions invalidated successfully");
                return true;
            }else{
                System.out.println("All active sessions invalidated unsuccessfully");
                return false;
            }
        }catch (Exception e){
            debuggingService.nonFatalDebug(e.getMessage());
            return false;
        }
    }

    private boolean ReloadAllTables() throws Exception{
        return tableManagerService.reloadAllTables();
    }

}
