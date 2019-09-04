package com.kirchnersolutions.database.Servers.socket;

import java.util.concurrent.Callable;

public class MsgThread implements Callable<String> {

    private String msg;
    private BackupClient backupClient;

    public MsgThread(String msg, BackupClient backupClient) {
        this.msg = msg;
        this.backupClient = backupClient;
    }

    @Override
    public String call() throws Exception {
        if (backupClient.isConnected()) {
            return backupClient.sendMessage(msg);
        }
        return "null";
    }
}