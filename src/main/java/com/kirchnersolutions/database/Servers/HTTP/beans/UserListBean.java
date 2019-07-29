package com.kirchnersolutions.database.Servers.HTTP.beans;

public class UserListBean {

    private String username = "", firstname = "", lastname = "", id = "", sessiontype = "", ip = "", admin = "", device = "", index = "", password = "";
    private int idi = -1;

    public String getIp() {
        return ip;
    }

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAdmin() {
        return admin;
    }

    public String getDevice() {
        return device;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getSessiontype() {
        return sessiontype;
    }

    public String getPassword() {
        return password;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
        if(!id.equals("")){
            this.idi = Integer.parseInt(id);
        }
    }

    public int getIdi() {
        //return Integer.parseInt(id);
        return idi;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setSessiontype(String sessiontype) {
        this.sessiontype = sessiontype;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString(){
        return "username:" + username + ";firstname:" + firstname + ";lastname:" + lastname + ";userid:" + id + ";admin:" + admin + ";index:" +
                index + ";ip:" + ip + ";session:" + sessiontype + ";device:" + device;
    }
}