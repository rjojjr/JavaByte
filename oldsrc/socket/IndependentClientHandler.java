/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.database.Servers.socket;


import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.SocketSession;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rjojj
 */
class IndependentClientHandler extends Thread {

    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    DataOutputStream dOut = null;
    DataInputStream dIn = null;

    private TransactionService transactionService;
    private TransactionSerializer transactionSerializer;
    private DebuggingService debuggingService;
    private DatabaseObjectFactory databaseObjectFactory;

    private SocketSession session;
    private SessionService sessionService;

    private volatile boolean loggedOn = false;
    private String userName = "";

    private boolean stop;

    public IndependentClientHandler(Socket socket, TransactionService transactionService,
                                    TransactionSerializer transactionSerializer, SocketSession session,
                                    DebuggingService debuggingService, SessionService sessionService,
                                    DatabaseObjectFactory databaseObjectFactory) throws Exception{
        this.clientSocket = socket;
        dOut = new DataOutputStream(socket.getOutputStream());
        dIn = new DataInputStream(socket.getInputStream());
        this.transactionService = transactionService;
        this.transactionSerializer = transactionSerializer;
        this.databaseObjectFactory = databaseObjectFactory;
        this.session = session;
        this.debuggingService = debuggingService;
        this.sessionService = sessionService;
        //System.out.println("here");
        stop = false;
    }

    public String getUserName() {
        synchronized (this) {
            return new String(userName);
        }
    }

    public void stopThread() {
        synchronized (this) {
            this.stop = true;
        }
    }

    @Override
    public void run() {
    String ip = clientSocket.getInetAddress().toString();
    int port = clientSocket.getPort();
        Thread.currentThread().setName("SocketSession port " + port + " address " + ip);
        session.setIp(ip);
        session.setPort(port);
        debuggingService.socketDebug("Socket client connected on port " + port);
        //transMan = TransactionManager.getInstance();
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException ex) {
            debuggingService.socketDebug("Failure to open client socket output on port " + port + " " + ex.getMessage());
            debuggingService.nonFatalDebug("Failure to open client socket output on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            debuggingService.socketDebug("Failure to open client socket input on port " + port + " " + ex.getMessage());
            debuggingService.nonFatalDebug("Failure to open client socket input on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String inputLine;
        int length;
        try {
            while ((inputLine = in.readLine()) != null) {

                out.write(new String(transactionService.submitTransaction((Transaction)databaseObjectFactory.databaseObjectFactory(inputLine.getBytes("UTF-8")), session), "UTF-8"));
                out.flush();
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            debuggingService.socketDebug("Failure to read client socket input on port " + port + " " + ex.getMessage());
            debuggingService.nonFatalDebug("Failure to read client socket input on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception es){
            debuggingService.socketDebug("Failure to read client socket input on port " + port + " " + es.getMessage());
            debuggingService.nonFatalDebug("Failure to read client socket input on port " + port + " " + es.getMessage());
        }

        try {
            clientSocket.close();
            debuggingService.socketDebug("Closed client socket on port " + port);
        } catch (Exception ex) {
            debuggingService.socketDebug("Failure to close client socket input on port " + port + " " + ex.getMessage());
            debuggingService.nonFatalDebug("Failure to close client socket input on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
