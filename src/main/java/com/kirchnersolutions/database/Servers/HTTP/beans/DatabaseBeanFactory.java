package com.kirchnersolutions.database.Servers.HTTP.beans;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.SessionException;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.WebSession;
import com.kirchnersolutions.utilities.CalenderConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class DatabaseBeanFactory {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private DevVars devVars;

    public LogonFormBean newLogonFormBean(HttpSession httpSession){
        WebSession session = sessionService.getNewSession(httpSession);
        if(session == null){
            //System.out.println("here");
        }
        LogonFormBean bean = new LogonFormBean();
        return bean;
    }

    public LogonFormBean newPasswordResetFormBean(WebSession session){
        LogonFormBean bean = new LogonFormBean();
        return bean;
    }

    public UserBean createUserBean(WebSession session) throws Exception{
        if(session.getUser() != null){
            User user = session.getUser();
            try{
                if(user.getDetail(new String("admin")).equals(new String("true"))){
                    return new UserBean(session, user.getDetail(new String("username")).toString(), user.getDetail(new String("id")).toString(), true);
                }
                return new UserBean(session, user.getDetail(new String("username")).toString(), user.getDetail(new String("id")).toString(), false);
            }catch (Exception e){
                if (devVars.isDevExceptions()) {
                    System.out.println("Corrupt database User object. Session ID: " + session.getSessionIndex() + " " + e.getMessage() + " Class DatabaseBeanFactory Method createuserBean");
                    throw new DevelopmentException("Corrupt database User object. Session ID: " + session.getSessionIndex() + " " + e.getMessage() + " Class DatabaseBeanFactory Method createuserBean");
                } else {
                    System.out.println("Corrupt database User object. Session ID: " + session.getSessionIndex() + " " + e.getMessage());
                    throw new SessionException("Corrupt database User object. Session ID: " + session.getSessionIndex() + " " + e.getMessage());
                }
            }

        }
        return null;
    }

    private TransactionBean getTransactionBean(Map<String, String> row){
       TransactionBean bean = new TransactionBean();
        bean.setUsername(row.get("user"));
        bean.setSuccess(row.get("success"));
        bean.setOperation(row.get("operation"));
        bean.setTime(CalenderConverter.getMonthDayYearHourMinuteSecond(Long.parseLong(row.get("time")), ";", ":"));
        return bean;
    }

}