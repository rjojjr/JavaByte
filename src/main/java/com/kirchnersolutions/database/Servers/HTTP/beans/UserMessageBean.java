package com.kirchnersolutions.database.Servers.HTTP.beans;

import java.util.ArrayList;
import java.util.List;

public class UserMessageBean {

    private List<String> userNames = new ArrayList<>();
    private String message = "", from;

    public UserMessageBean(String from){
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public void setUser(String username){
        userNames.add(username);
    }

    public List<String> getUserNames() {
        return userNames;
    }
}