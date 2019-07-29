package com.kirchnersolutions.database.Servers.socket;

import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.SocketSession;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.annotation.ApplicationScope;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
class SocketServer extends Thread{

    private volatile ServerSocket serverSocket;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private TransactionService transactionService;
    private TransactionSerializer transactionSerializer;
    private SessionService sessionService;
    private DebuggingService debuggingService;
    private DatabaseObjectFactory databaseObjectFactory;

    private int port = 0;
    private volatile AtomicBoolean running = new AtomicBoolean(false);

    /*
    public SocketServer(ThreadPoolTaskExecutor threadPoolTaskExecutor, TransactionService transactionService, TransactionSerializer transactionSerializer, SessionService sessionService, DebuggingService debuggingService){
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

     */

    void setPort(int nPort){
        port = nPort;
    }
/*
    public void run(){
        Thread.currentThread().setName("SocketServer port " + port);
        //running.set(true);
        try{
            startServer(port);
        }catch (Exception e){
            running.set(false);
        }
    }

 */

    public void manualStart() throws Exception{
        try {
            running.set(true);
            serverSocket = new ServerSocket();
            InetAddress inetAddress = InetAddress.getLocalHost();
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(inetAddress.getHostAddress()), port));
            debuggingService.socketDebug("Socket server manually started on port " + port);
            while (true) {
                IndependentClientHandler connection = new IndependentClientHandler(serverSocket.accept(), transactionService, transactionSerializer, (SocketSession)(sessionService.getNewSession("socket")), debuggingService, sessionService, databaseObjectFactory);
                connection.start();
            }
        } catch (Exception ex) {
            running.set(false);
            debuggingService.socketDebug("Failed to manually start socket server on port " + port + ex.getMessage());
            debuggingService.throwDevException(new DevelopmentException("Failed to manually start socket server on port " + port + " " + ex.getMessage()));
            debuggingService.nonFatalDebug("Failed to manually start socket server on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start(int port) {
        try {
            running.set(true);
            serverSocket = new ServerSocket();
            InetAddress inetAddress = InetAddress.getLocalHost();
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(inetAddress.getHostAddress()), port));
        //serverSocket = new ServerSocket(port, 1, InetAddress.getByName(inetAddress.getHostAddress()));
            debuggingService.socketDebug("Socket server started on port " + port);
            while (true) {
                IndependentClientHandler connection = new IndependentClientHandler(serverSocket.accept(), transactionService, transactionSerializer, (SocketSession)(sessionService.getNewSession("socket")), debuggingService, sessionService, databaseObjectFactory);
                connection.start();
                //threadPoolTaskExecutor.execute(new IndependentClientHandler(serverSocket.accept(), transactionService, transactionSerializer, (SocketSession)(sessionService.getNewSession("socket")), debuggingService, sessionService, databaseObjectFactory));
            }
        } catch (Exception ex) {
            running.set(false);
            debuggingService.socketDebug("Failed to start socket server on port " + port + ex.getMessage());
            //debuggingService.throwDevException(new DevelopmentException("Failed to start socket server on port " + port + " " + ex.getMessage()));
            debuggingService.nonFatalDebug("Failed to start socket server on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stopServer() throws Exception{
        try {
            serverSocket.close();
            running.set(false);
            debuggingService.socketDebug("Socket server stopped on port " + port);
        } catch (Exception ex) {
            ex.printStackTrace();
            debuggingService.socketDebug("Failed to start socket server on port " + port);
            debuggingService.throwDevException(new DevelopmentException("Failed to stop socket server on port " + port + " " + ex.getMessage()));
            debuggingService.nonFatalDebug("Failed to stop socket server on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}