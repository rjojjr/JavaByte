package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Servers.HTTP.beans.StompRegisterRequest;
import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableRequest;
import com.kirchnersolutions.database.dev.DebugLogRepository;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LogStompService {

    @Autowired
    private DebugLogRepository debugLogRepository;
    @Autowired
    private volatile SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private IPLogger ipLogger;

    void searchLogs(StompTableRequest request){
        Map<String, String> query = new HashMap<>();
        if(request.getQuery().equals("")){

        }else{
            String[] terms = request.getQuery().split(",");
            for(String req : terms){
                String[] temp = req.split(":");
                query.put(temp[0], temp[1]);
            }
        }
        this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", debugLogRepository.searchTraceLogs(query, request.getTablename()), createHeaders(request.getSessionid()));
    }

    void getIPLogNumber(StompRegisterRequest request){
        debuggingService.stompDebug("@Stomp IP Logs count requested by: " + request.getUsername());
        this.simpMessagingTemplate.convertAndSendToUser(request.getSession(), "/queue/notify", "num%" + getIPLogsNumber(), createHeaders(request.getSession()));
    }

    private String getIPLogsNumber(){
        return ipLogger.getNumerLogs();
    }

    void dumpIPLogs(StompRegisterRequest request) throws Exception{
        try{
            debuggingService.stompDebug("@Stomp Dump IP Logs initiated by: " + request.getUsername());
            ipLogger.dumpLogs();
            this.simpMessagingTemplate.convertAndSendToUser(request.getSession(), "/queue/notify", "dump", createHeaders(request.getSession()));
        }catch (Exception e){
            debuggingService.stompDebug("@Stomp Dump IP Logs initiated by: " + request.getUsername() + " failed");
            debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
            debuggingService.nonFatalDebug(e.getMessage());
        }
    }

    void dumpLogs(String type, String stompId) throws Exception{
        try{
            String suc = "false";
            if(debuggingService.dumpLogs(type)){
                suc = "true";
            }
            this.simpMessagingTemplate.convertAndSendToUser(stompId, "/queue/notify", "dump%" + suc, createHeaders(stompId));
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
            debuggingService.nonFatalDebug(e.getMessage());
        }
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
