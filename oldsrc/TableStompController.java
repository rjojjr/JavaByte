package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableInfo;
import com.kirchnersolutions.database.Servers.HTTP.beans.StompTableRequest;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@DependsOn("transactionService")
public class TableStompController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private DevVars devVars;
    @Autowired
    private HTTPService httpService;
    @Autowired
    private TableStompService tableStompService;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private TransactionService transactionService;

    @MessageMapping("/table/info")
    public void getTableInfo(StompTableRequest request){
        tableStompService.getTableInfo(request);
    }

}
