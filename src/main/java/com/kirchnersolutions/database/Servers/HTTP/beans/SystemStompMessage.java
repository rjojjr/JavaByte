package com.kirchnersolutions.database.Servers.HTTP.beans;

public class SystemStompMessage {

    private String index = "-1";
    private String page = "";

    public String getIndex() {
        return index;
    }

    public String getPage() {
        return page;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setPage(String page) {
        this.page = page;
    }
}