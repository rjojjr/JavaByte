package com.kirchnersolutions.database.sessions;

import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.Servers.HTTP.beans.UserBean;

import javax.servlet.http.HttpSession;

public class WebSession extends Session {

    private volatile HttpSession httpSession = null;
    private volatile UserBean userBean = null;
    private volatile String stompID = "";

    public WebSession(){
        super(new String("web"));
    }

    public WebSession(HttpSession httpSession){
        super("web");
        this.httpSession = httpSession;
        if(httpSession != null){
            httpSession.setAttribute("session", this);
        }
    }

    public HttpSession getHttpSession(){
        if(user != null){
            //httpSession.setAttribute("user", user);
        }
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
        if(httpSession != null){
            httpSession.setAttribute("session", this);
            if(user != null){
                //httpSession.setAttribute("user", user);
            }
        }
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public void setDeviceCertificate(DeviceCertificate deviceCertificate) {
        setDevice(deviceCertificate);
    }

    public String getStompID() {
        return stompID;
    }

    public void setStompID(String stompID) {
        this.stompID = stompID;
    }

    public UserBean getUserBean(){
        return userBean;
    }

}