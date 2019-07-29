package com.kirchnersolutions.database.Servers.HTTP.beans;

public class IPBean {

    private String ip = "";
    private boolean refresh = false;

    public IPBean(String ip){
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }
}