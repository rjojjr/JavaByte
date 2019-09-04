package com.kirchnersolutions.database;

import com.kirchnersolutions.database.Servers.socket.MultiClientServer;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final Logger LOG =
            LoggerFactory.getLogger(AppStartupRunner.class);

    public static int counter;

    @Autowired
    private volatile ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private volatile TransactionService transactionService;
    @Autowired
    private TransactionSerializer transactionSerializer;
    @Autowired
    private volatile SessionService sessionService;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private DatabaseObjectFactory databaseObjectFactory;

    private MultiClientServer multiClientServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /*
        System.out.println("ServerService bean created");
        this.multiClientServer = MultiClientServer.getInstance(threadPoolTaskExecutor, transactionService, transactionSerializer,
                sessionService, debuggingService, databaseObjectFactory);
        if(!multiClientServer.isRunning()){
            multiClientServer.start();
        }

         */
    }
}
