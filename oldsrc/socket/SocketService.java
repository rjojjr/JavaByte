package com.kirchnersolutions.database.Servers.socket;

import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;

@ApplicationScope
@Service
//@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
//@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SocketService {

    @Autowired
    private volatile ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private  TransactionService transactionService;
    @Autowired
    private  TransactionSerializer transactionSerializer;
    @Autowired
    private  SessionService sessionService;
    @Autowired
    private  DebuggingService debuggingService;
    @Autowired
    private DatabaseObjectFactory databaseObjectFactory;

    private volatile MultiClientServer multiClientServer;

    public SocketService() throws Exception{

    }

    @PostConstruct
    public void init() throws Exception{
        this.multiClientServer = MultiClientServer.getInstance(threadPoolTaskExecutor, transactionService, transactionSerializer,
                sessionService, debuggingService, databaseObjectFactory);
        multiClientServer.start();

    }

    public String getStats() {
        try{
            if(multiClientServer.isRunning()){
                return "true," + multiClientServer.getPort();
            }else {
               // multiClientServer.manualStart();
                return "false";

            }
        }catch (Exception e){
            return e.getMessage();
        }
    }

    public boolean startServer(){
        try{
            if(!multiClientServer.isRunning()){
                multiClientServer.manualStart();
                return multiClientServer.isRunning();
            }
            return multiClientServer.isRunning();
        }catch (Exception e){
            return false;
        }
    }

    public boolean stop(){
        try{
            if(multiClientServer.isRunning()){
                multiClientServer.stop();
                return !multiClientServer.isRunning();
            }
            return !multiClientServer.isRunning();
        }catch (Exception e){
            return false;
        }
    }
}
