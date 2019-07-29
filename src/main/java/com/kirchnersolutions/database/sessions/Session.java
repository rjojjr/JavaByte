package com.kirchnersolutions.database.sessions;

import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.objects.User;
import org.springframework.beans.factory.annotation.Autowired;

public class Session {


    private String type, device;
    User user = null;
    private long lastActivity = 0;
    private int sessionIndex = -1;
    private String ip = "";
    private volatile DeviceCertificate deviceCertificate = null;

    public Session (String type){
        lastActivity = System.currentTimeMillis();
        this.type = type;
    }

    public Session(){
        lastActivity = System.currentTimeMillis();
    }


    public String getDeviceType(){
        lastActivity = System.currentTimeMillis();
        return deviceCertificate.getType();
    }

    public void setDevice(DeviceCertificate deviceCertificate){
        lastActivity = System.currentTimeMillis();
        this.deviceCertificate = deviceCertificate;
    }

    /**
     * Sets current ipv4 address of this session
     * @param ip
     */
    public void setIp(String ip){
        this.ip = ip;
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Returns last known ip of this session.
     * @return
     */
    public String getIp(){

        return new String(ip);
    }

    public int getSessionIndex(){
        return sessionIndex;
    }

    public String getUserIndex(){
        return user.getDetail(new String("index"));
    }

    /**
     * For session repository to set.
     * @param sessionIndex
     */
    void setIndex(int sessionIndex){
        this.sessionIndex = sessionIndex;
    }

    public String getType(){
        return new String(type);
    }

    /**
     * Returns session user.
     * @return
     */
    public User getUser(){
        return user;
    }

    public void setUser(User user) {
        lastActivity = System.currentTimeMillis();
        this.user = user;
    }

    /**
     * Returns the time of users last activity.
     * @return
     */
    long getLastActivity() {
        return lastActivity;
    }

    String getDetail(String key)
    {
        lastActivity = System.currentTimeMillis();
        return GetDetail(key);
    }

    public DeviceCertificate getDeviceCertificate(){
        lastActivity = System.currentTimeMillis();
        return deviceCertificate;
    }
    /**
     * Updates the time of the last user activity.
     */
    void updateTime(){
        lastActivity = System.currentTimeMillis();
    }



    private String GetDetail(String key){
        if (key.equals(new String("username"))) {
            return getUser().getDetail(new String("username"));
        }
        if (key.equals(new String("firstname"))) {
            return getUser().getDetail(new String("firstname"));
        }
        if (key.equals(new String("lastname"))) {
            return getUser().getDetail(new String("lastname"));
        }
        if (key.equals(new String("id"))) {
            return getUser().getDetail(new String("id"));
        }
        if (key.equals(new String("admin"))) {
            return getUser().getDetail(new String("admin"));
        }
        if (key.equals(new String("index"))) {
            return getUser().getDetail(new String("index"));
        }
        if (key.equals(new String("ip"))) {
            if(type.equals(new String("web"))){
                return new String(getIp());
            }else{
                return new String("null");
            }
        }
        if (key.equals(new String("type"))) {
            if(type.equals(new String("web"))){
                return new String("web");
            }else{
                return new String("null");
            }
        }
        if (key.equals(new String("device"))) {
            if(type.equals(new String("web"))){
                return getDeviceType();
            }else{
                return new String("null");
            }
        }
        return null;
    }

}