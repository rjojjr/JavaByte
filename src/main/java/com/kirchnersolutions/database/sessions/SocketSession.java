package com.kirchnersolutions.database.sessions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketSession extends Session {

    private int port;
    private String ip;

    public SocketSession(){
        super(new String("socket"));
    }



}