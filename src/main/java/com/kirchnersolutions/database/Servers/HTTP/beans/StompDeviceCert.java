package com.kirchnersolutions.database.Servers.HTTP.beans;

public class StompDeviceCert {

    private String cert = "", user = "", ip = "";

    public String getCert() {
        return cert;
    }

    public String getUser() {
        return user;
    }

    public String getIp() {
        return ip;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}