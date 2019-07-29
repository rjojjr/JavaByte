package com.kirchnersolutions.database.Servers.HTTP.beans;

import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.WebSession;

public class DatabaseBean {

    private WebSession session;

    public DatabaseBean(){


    }

    public void setSession(WebSession session) {
        if(session == null){
            System.out.println("Super null");
        }
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}