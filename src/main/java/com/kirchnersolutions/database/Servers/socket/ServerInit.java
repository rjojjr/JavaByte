package com.kirchnersolutions.database.Servers.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServerInit {

    //@Autowired
    //SocketService socketService;

    @PostConstruct
    protected void init() throws Exception{
        //@Autowired
        //socketService = new SocketService();
    }

}
