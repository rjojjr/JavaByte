/*
 * kData Performance Database 
 * 
 * Version: 1.0.00b
 * 
 * Robert Kirchner Jr.
 * 2018 Kirchner Solutions
 * 
 * This code is not to be distributed, compiled, decompiled
 * copied, used, recycled, moved or modified in any way without 
 * express written permission from Kirchner Solutions
 */
package com.kirchnersolutions.database.Servers.socket;

import com.kirchnersolutions.database.Configuration.SocketServerConfiguration;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.SocketSession;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * MultiClientServer Version 1.0.00b
 *
 * @author Robert Kirchner Jr. 2018 Kirchner Solutions
 */
public class MultiClientServer {

    private volatile ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private volatile TransactionService transactionService;
    private TransactionSerializer transactionSerializer;
    private volatile SessionService sessionService;
    private DebuggingService debuggingService;
    private DatabaseObjectFactory databaseObjectFactory;

    private volatile int port = 0;
    private volatile static AtomicBoolean running = new AtomicBoolean(false);
    private volatile static SocketServer server;

    private static volatile MultiClientServer single_insatance = null;

    private MultiClientServer(ThreadPoolTaskExecutor threadPoolTaskExecutor, TransactionService transactionService, TransactionSerializer transactionSerializer,
                             SessionService sessionService, DebuggingService debuggingService, DatabaseObjectFactory databaseObjectFactory) throws Exception{
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        System.out.println("Server bean created");
        this.transactionService = transactionService;
        this.transactionSerializer = transactionSerializer;
        this.databaseObjectFactory = databaseObjectFactory;
        this.sessionService = sessionService;
        this.debuggingService = debuggingService;
        this.port = debuggingService.getSocketPort();
        server = new SocketServer(new ServerSocket(), threadPoolTaskExecutor, transactionService, transactionSerializer,
               sessionService, debuggingService, databaseObjectFactory, port, running);
        //start();

    }

    public static MultiClientServer getInstance(ThreadPoolTaskExecutor threadPoolTaskExecutor, TransactionService transactionService, TransactionSerializer transactionSerializer,
                              SessionService sessionService, DebuggingService debuggingService, DatabaseObjectFactory databaseObjectFactory) throws Exception{
        if(single_insatance == null){
            single_insatance = new MultiClientServer(threadPoolTaskExecutor, transactionService, transactionSerializer,
                    sessionService, debuggingService, databaseObjectFactory);
        }
        return single_insatance;
    }

    public synchronized boolean isRunning(){
        return server.isRunning();
    }

   void manualStart() throws Exception{
        if(!isRunning()){
            threadPoolTaskExecutor.execute(server);
        }
    }

    public void start() throws Exception{
        if(!isRunning()){
            ///threadPoolTaskExecutor.execute(server);
            try{
                threadPoolTaskExecutor.execute(server);
                running.set(true);
                //threadPoolTaskExecutor.execute(new SocketServer(new ServerSocket(), threadPoolTaskExecutor, transactionService, transactionSerializer,
                       //sessionService, debuggingService, databaseObjectFactory, port, running));
            }catch (Exception e){
                running.set(false);
                debuggingService.socketDebug(e.getMessage());
                debuggingService.nonFatalDebug(debuggingService.getStack(e));
            }

        }
    }

    void stop() throws Exception{
        if(isRunning()){
            server.stop();
        }
    }

    int getPort(){
        return port;
    }



}

