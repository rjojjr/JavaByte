package com.kirchnersolutions.database.Servers.HTTP.beans;

import java.math.BigInteger;

public class DeviceCertificate {

    private BigInteger cert, index = new BigInteger("-1");
    private String username = new String("null"), type = new String("pc");
    private String ip = "null";

    public DeviceCertificate(BigInteger cert){
        this.cert = cert;
    }

    public BigInteger getCert() {
        return new BigInteger(cert.toByteArray());
    }

    public void setIndex(BigInteger index) {
        this.index = index;
    }

    public BigInteger getIndex() {
        return index;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}