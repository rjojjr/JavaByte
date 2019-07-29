package com.kirchnersolutions.database.Servers.HTTP.beans;

public class StompRegisterRequest {

    private String username = "", session = "";

    public StompRegisterRequest(String username, String session){
        this.username = username;
        //Stomp ID.
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public String getSession() {
        return session;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSession(String session) {
        this.session = session;
    }
}