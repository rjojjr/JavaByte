/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirchnersolutions.database.Servers.socket;


import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.SocketSession;
import com.kirchnersolutions.utilities.SerialService.TransactionSerializer;

import java.math.BigInteger;
import java.util.Base64;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rjojj
 */
class IndependentClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedWriter out;
    private BufferedReader in;
    

    private TransactionService transactionService;
    private TransactionSerializer transactionSerializer;
    private DebuggingService debuggingService;
    private DatabaseObjectFactory databaseObjectFactory;

    private SocketSession session;
    private SessionService sessionService;

    private volatile boolean loggedOn = false;
    private String userName = "";

    private boolean stop;

    public IndependentClientHandler(Socket socket, TransactionService transactionService, TransactionSerializer transactionSerializer, SocketSession session, DebuggingService debuggingService, SessionService sessionService, DatabaseObjectFactory databaseObjectFactory) {
        this.clientSocket = socket;
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
    String ip = clientSocket.getRemoteSocketAddress().toString();
    int port = clientSocket.getPort();
        Thread.currentThread().setName("SocketSession port " + port + " address " + ip);
        session.setIp(ip);
        session.setPort(port);
        DeviceCertificate dev = new DeviceCertificate(new BigInteger("0"));
        dev.setIp(ip);
        dev.setType("socket");
        session.setDevice(dev);
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
        try {
            while ((inputLine = in.readLine()) != null) {
                //debuggingService.socketDebug("Input: " + inputLine);
                if(session.getUser() != null){
                    dev.setUsername(session.getUser().getDetail("username"));
                }
                String output = new String(transactionService.submitTransaction((Transaction)databaseObjectFactory.databaseObjectFactory(Base64.getDecoder().decode(inputLine.getBytes("UTF-8"))), session), "UTF-8");
                output = new String(Base64.getEncoder().encode(output.getBytes("UTF-8")), "UTF-8");
                out.write(output + "\n");
                dev.setUsername(session.getUser().getDetail("username"));
                out.flush();
                //debuggingService.socketDebug("Output: " + output);
            }
            in.close();
            out.close();
        } catch (IOException ex) {
            debuggingService.socketDebug("Failure to read client socket input on port " + port + " " + ex.getMessage());
            debuggingService.nonFatalDebug("Failure to read client socket input on port " + port + " " + ex.getMessage());
            Logger.getLogger(MultiClientServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception es){

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
