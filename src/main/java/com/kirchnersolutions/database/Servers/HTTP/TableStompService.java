package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableInfo;
import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableRequest;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.objects.DatabaseObjectFactory;
import com.kirchnersolutions.database.objects.DatabaseResults;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.WebSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
public class TableStompService {

    @Autowired
    private volatile SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    DatabaseObjectFactory databaseObjectFactory;

    void getTableInfo(StompTableRequest request){
        debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request information for table " + request.getTablename());
        String result = GetTableInfo(request);
        this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", result, createHeaders(request.getSessionid()));
    }

    void createTable(StompTableRequest request) throws Exception{
        debuggingService.stompDebug("@Stomp User " + request.getUsername() + " session id " + request.getSessionid() + " request to create table " + request.getTablename());
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            Transaction transaction = new Transaction();
            transaction.setUsername(request.getTablename());
            transaction.setRequestTime(System.currentTimeMillis());
            transaction.setOperation("CREATE TABLE " + request.getTablename() + " " + request.getQuery());
            transactionService.submitTransaction(transaction, session);
            if(transaction.isSuccessfull()){
                this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "10", createHeaders(request.getSessionid()));
            }else {
                this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "3%System%Operation failed", createHeaders(request.getSessionid()));
            }
        }else{
            this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "3%System%Operation failed due to improper credentials", createHeaders(request.getSessionid()));
        }

    }

    void queryTable(StompTableRequest request) throws Exception{
        String result = "";
        boolean first = true;
        debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request query for table " + request.getTablename());
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        Transaction transaction = new Transaction();
        transaction.setUsername(request.getUsername());
        transaction.setUserIndex(new BigInteger(session.getUserIndex()));
        Map<String, String> where = new HashMap<>();
        if(!request.getQuery().equals("all")){
            String[] fields = request.getQuery().split("%");
            for(String field : fields){
                String[] temp = field.split(";");
                where.put(temp[0], temp[1]);
            }
        }
        transaction.setOperation("SELECT ADVANCED " + request.getTablename());
        transaction.setWhere(where);
        transaction.setHowMany(new BigInteger("-1"));
        //
        DatabaseResults res = (DatabaseResults)databaseObjectFactory.databaseObjectFactory(transactionService.submitTransaction(transaction, session));
        //System.out.println(res.getMessage());
        if(res.isSuccess()){
            for(Map<String, String> map : res.getResults()){
                String temp = "";
                boolean tfirst = true;
                if(first){
                    for(String key : map.keySet()){
                        if(tfirst){
                            result = key;
                            temp = map.get(key);
                            tfirst = false;
                        }else{
                            result+= ":" + key;
                            temp+= ":" + map.get(key);
                        }
                    }
                    result+= ";" + temp;
                    first = false;
                }else{
                    for(String key : map.keySet()){
                        if(tfirst){
                            temp = map.get(key);
                            tfirst = false;
                        }else{
                            temp+= ":" + map.get(key);
                        }
                    }
                    result+= ";" + temp;
                }
            }
            result = "result%" + result;
            debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request query for table " + request.getTablename() + " sucessfull.");
            this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", result, createHeaders(request.getSessionid()));
        }else{

            debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request query for table " + request.getTablename() + " unsucessfull " + res.getMessage());
            this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "result%" + res.getMessage(), createHeaders(request.getSessionid()));
        }
    }

    private String GetTableInfo(StompTableRequest request){
        debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request stats for table " + request.getTablename());
        StompTableInfo result = tableManagerService.stompTableStats(request.getTablename());
        if(result == null){
            result = new StompTableInfo();
            result.setTablename("Error");
            debuggingService.stompDebug("@Stomp user " + request.getUsername() + " session id " + request.getSessionid() + " request information for table " + request.getTablename() + " failed");
            return "stat%" + result.toString().replaceAll(",", "%");
        }
        return "stat%" + result.toString().replaceAll(",", "%");
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}
