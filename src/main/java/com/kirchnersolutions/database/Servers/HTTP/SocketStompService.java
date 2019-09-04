package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Servers.socket.BackupClient;
import com.kirchnersolutions.database.Servers.socket.MsgThread;
import com.kirchnersolutions.database.Servers.socket.MultiClientServer;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@DependsOn({"debuggingService", "multiClientServer"})
@Service
public class SocketStompService {

    @Autowired
    private MultiClientServer multiClientServer;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    void getSocketStat(String stompID){
        String result = "socket%" + multiClientServer.getStats();
        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", result, createHeaders(stompID));
    }

    void shutdownSocket(String stompID) {
        try{
            if(multiClientServer.stop()){
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server shutdown successful", createHeaders(stompID));
                getSocketStat(stompID);
            }else{
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server shutdown unsuccessful", createHeaders(stompID));
                getSocketStat(stompID);
            }
        }catch (Exception e){
            debuggingService.socketDebug("Failed to shutdown socket server.");
            debuggingService.stompDebug("@Stomp Failed to shutdown socket server.");
            debuggingService.nonFatalDebug("Failed to shutdown socket server." + e.getStackTrace().toString());
        }

    }

    void startSocket(String stompID){
        try{
            if(multiClientServer.start()){
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server startup successful", createHeaders(stompID));
                getSocketStat(stompID);
            }else{
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server startup unsuccessful", createHeaders(stompID));
                getSocketStat(stompID);
            }
        }catch (Exception e){
            debuggingService.socketDebug("Failed to start socket server.");
            debuggingService.stompDebug("@Stomp Failed to start socket server.");
            debuggingService.nonFatalDebug("Failed to start socket server." + Arrays.toString(e.getStackTrace()));
        }
    }

    void backUpTables(String stompID) throws Exception{
        debuggingService.stompDebug("@Stomp Database backup requested by session: " + stompID);
        BackupClient bk1 = new BackupClient();
        BackupClient bk2 = new BackupClient();
        debuggingService.socketDebug("Connecting to socket server at address 192.168.1.175 port 4431");
        bk1.startConnection("192.168.1.175", 4431);
        debuggingService.socketDebug("Connected to socket server at address 192.168.1.175 port 4431");
        debuggingService.socketDebug("Connecting to socket server at address 192.168.1.215 port 4432");
        bk1.startConnection("192.168.1.215", 4431);
        debuggingService.socketDebug("Connected to socket server at address 192.168.1.215 port 4432");
        List<String> tables = tableManagerService.getTableNames();
        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Generating table checksums", createHeaders(stompID));
        boolean hashb = true;
        for(int i = 0; i < tables.size(); i++){
            String hash = "h;" + tables.get(i);
            i++;
            if(i < tables.size()){
                Future<String> fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
                Future<String> fut2 = threadPoolTaskExecutor.submit(new MsgThread("h;" + tables.get(i), bk2));
                String res = fut1.get();
                if(res.equals("null")){
                    this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table checksums", createHeaders(stompID));
                    hashb = false;
                    break;
                }
                res = fut2.get();
                if(res.equals("null")){
                    this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table checksums", createHeaders(stompID));
                    hashb = false;
                    break;
                }
            }else{
                Future<String> fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
                String res = fut1.get();
                if(res.equals("null")){
                    this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table checksums", createHeaders(stompID));
                    hashb = false;
                    break;
                }
            }
        }
        if(hashb){
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Generating table backups", createHeaders(stompID));
            for(int i = 0; i < tables.size(); i++){
                String hash = "bk;" + tables.get(i);
                i++;
                if(i < tables.size()){
                    Future<String> fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
                    Future<String> fut2 = threadPoolTaskExecutor.submit(new MsgThread("bk;" + tables.get(i), bk2));
                    String res = fut1.get();
                    if(res.equals("null")){
                        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table backups", createHeaders(stompID));
                        hashb = false;
                        break;
                    }
                    res = fut2.get();
                    if(res.equals("null")){
                        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table backups", createHeaders(stompID));
                        hashb = false;
                        break;
                    }
                }else{
                    Future<String> fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
                    String res = fut1.get();
                    if(res.equals("null")){
                        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table backups", createHeaders(stompID));
                        hashb = false;
                        break;
                    }
                }
            }
        }
        bk1.stopConnection();
        bk2.stopConnection();
        if(hashb){
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Backup finished successfully", createHeaders(stompID));
        }
    }

    void backUpTable(String stompID, String table) throws Exception {
        debuggingService.stompDebug("@Stomp Database backup requested by session: " + stompID);
        BackupClient bk1 = new BackupClient();
        BackupClient bk2 = new BackupClient();
        debuggingService.socketDebug("Connecting to socket server at address 192.168.1.175 port 4431");
        bk1.startConnection("192.168.1.177", 4431);
        debuggingService.socketDebug("Connected to socket server at address 192.168.1.175 port 4431");
        debuggingService.socketDebug("Connecting to socket server at address 192.168.1.215 port 4432");
        bk1.startConnection("192.168.1.215", 4431);
        debuggingService.socketDebug("Connected to socket server at address 192.168.1.215 port 4432");
        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Generating table checksum", createHeaders(stompID));
        List<String> tables = tableManagerService.getTableNames();
        boolean hashb = true;
        if(tables.contains(table)){
            String hash = "h;" + table;
            Future<String> fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
            String res = fut1.get();
            if (res.equals("null")) {
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table checksums", createHeaders(stompID));
                hashb = false;
            }else{
                this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Generating table backup", createHeaders(stompID));
                hash = "bk;" + table;
                fut1 = threadPoolTaskExecutor.submit(new MsgThread(hash, bk1));
                res = fut1.get();
                if (res.equals("null")) {
                    this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Failed to generate table backup", createHeaders(stompID));
                    hashb = false;
                }else {
                    this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "status%Backup finished successfully", createHeaders(stompID));
                }
            }
        }
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
