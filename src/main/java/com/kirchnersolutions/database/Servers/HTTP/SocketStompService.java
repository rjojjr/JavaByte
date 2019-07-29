package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Servers.socket.SocketService;
import com.kirchnersolutions.database.dev.DebuggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@DependsOn({"debuggingService", "socketService"})
@Service
public class SocketStompService {

    @Autowired
    private SocketService socketService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private DebuggingService debuggingService;

    void getSocketStat(String stompID){
        String result = "socket%" + socketService.getStats();
        this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", result, createHeaders(stompID));
    }

    void shutdownSocket(String stompID){
        if(socketService.stop()){
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server shutdown successful", createHeaders(stompID));
            getSocketStat(stompID);
        }else{
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server shutdown unsuccessful", createHeaders(stompID));
            getSocketStat(stompID);
        }
    }

    void startSocket(String stompID){
        if(socketService.startServer()){
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server startup successful", createHeaders(stompID));
            getSocketStat(stompID);
        }else{
            this.simpMessagingTemplate.convertAndSendToUser(stompID, "/queue/notify", "3%System%Socket server startup unsuccessful", createHeaders(stompID));
            getSocketStat(stompID);
        }
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
