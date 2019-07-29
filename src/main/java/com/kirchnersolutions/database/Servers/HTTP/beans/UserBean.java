package com.kirchnersolutions.database.Servers.HTTP.beans;

import com.kirchnersolutions.database.sessions.WebSession;

import java.math.BigInteger;

public class UserBean extends DatabaseBean{

    private String username = "", id = "", msg = "";
    private boolean admin = false, resultPage = false;
    private BigInteger password;
    private WebSession session;

    public UserBean(WebSession session, String username, String id, boolean admin){
        super();
        this.id = id;
        this.username = username;
        this.admin = admin;
        setSession(session);
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isResultPage() {
        return resultPage;
    }

    public void setResultPage(boolean resultPage) {
        this.resultPage = resultPage;
    }
}