package com.kirchnersolutions.database.sessions;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Servers.HTTP.DeviceService;
import com.kirchnersolutions.database.Servers.HTTP.beans.DatabaseBeanFactory;
import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.Servers.HTTP.beans.LogonFormBean;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.SessionException;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.utilities.CryptTools;
import com.kirchnersolutions.database.core.tables.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScope
@DependsOn({"sessionRepository", "tableManagerService", "transactionService"})
@Component

public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private DevVars devVars;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private DatabaseBeanFactory databaseBeanFactory;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DebuggingService debuggingService;

    /**
     * Returns new session of given type.
     * Returns null if type is invalid.
     * @param type
     * @return
     */
    public Session getNewSession(String type){
        // type = web or socket.
        return GetNewSession(type);
    }

    /**
     * Returns new web session with give http session.
     * @param httpSession
     * @return
     */
    public WebSession getNewSession(HttpSession httpSession){
        // type = web or socket.
        return GetNewWebSession(httpSession);
    }

    /**
     * Invalidates user session if it exists.
     * User must have admin privilge.
     * @param admin
     * @param targetSessionUsername
     * @return
     * @throws Exception
     */
    public boolean invalidateSession(User admin, String targetSessionUsername) throws Exception{
        return InvalidateSession(admin, targetSessionUsername);
    }

    /**
     * Returns "true" if logon success, returns reason for failure if not.
     * Validates that passwords match, and that the password contains a capitol letter, a number and a special character(!, #, %).
     * @Validate
     * @param form
     * @param session
     * @return
     */
    public String logon(LogonFormBean form, WebSession session) throws Exception{
        return Logon(form, session);
    }

    public Session getSessionByID(String id){
        return GetSessionByID(id);
    }

    public boolean logOff(int action, Session session) throws Exception{
        return LogOff(action, session);
    }

    /**
     * Returns list of active sessions.
     * Returns null if user is not admin.
     * @param user
     * @return
     */
    public List<Session> getActiveSessions(User user){
        return GetActiveSessions(user);
    }

    public boolean shutdown(Session admin) throws Exception{
        return Shutdown(admin);
    }

    /**
     * Sets user's stomp ID.
     * Returns false if user has no web session.
     * @param username
     * @param stompID
     * @return
     */
    public boolean setStompID(String username, String stompID){
        return SetStompID(username, stompID);
    }

    /**
     * Gets user's stomp ID.
     * Returns null if user has no web session.
     * @param username
     * @return
     */
    public String getStompID(String username){
        return GetStompID(username);
    }

    public Session getSessionByUsername(String username){
        return GetSessionByUsername(username);
    }

    private boolean SetStompID(String username, String stompID){
        return sessionRepository.setStompID(username, stompID);
    }

    private String GetStompID(String username){
        return sessionRepository.getStompID(username);
    }

    private Session GetNewSession(String type){
        Session session = SessionFactory.sessionFactory(type);
        sessionRepository.addSession(session);
        return session;
    }

    public SocketSession getNewSocketSession(){
        SocketSession session = (SocketSession)SessionFactory.sessionFactory("socket");
        sessionRepository.addSession(session);
        return session;
    }

    private WebSession GetNewWebSession(HttpSession httpSession){
        WebSession session = new WebSession(httpSession);
        httpSession.setAttribute("session", session);
        //sessionRepository.addSession(session);
        return session;
    }

    private List<Session> GetActiveSessions(User user){
        return sessionRepository.getActiveSessions(user);
    }

    private Session GetSessionByUsername(String username){
        return sessionRepository.getSessionByUsername(username);
    }

    private Session GetSessionByID(String id){
        return sessionRepository.getSessionByID(id);
    }

    private String Logon(LogonFormBean form, WebSession session) throws Exception{
        String password = form.getPassword();
        boolean specialChar = false, capitol = false, number = false, failed = true;
        char[] chars = password.toCharArray();
        for(char c : chars){
            boolean Continue = true;
            if(!specialChar){
                if(c == '!' || c == '#' || c == '%'){
                    specialChar = true;
                    Continue = false;
                }
            }
            if(Continue && !capitol){
                if(!Character.isDigit(c) && Character.isLetter(c) && !Character.isLowerCase(c)){
                    capitol = true;
                    Continue = false;
                }
            }
            if(Continue && !number){
                if(Character.isDigit(c)){
                    number = true;
                    Continue = false;
                }
            }
            if(specialChar && capitol && number){
                failed = false;
                break;
            }
        }
        if(failed){
            System.out.println("Improper credentials.");
            return "Improper password format";
        }
        Transaction transaction = new Transaction();
        transaction.setOperation(new String("LOGON"));
        BigInteger intPassword = new BigInteger("-1");
        try{
            intPassword = new BigInteger(CryptTools.getSHA256(password));
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException("Failed to hash password. Session ID: " + session.getSessionIndex() + " " + e.getMessage() + " Class SessionService Method Logon"));
            debuggingService.nonFatalDebug("Failed to hash password. Session ID: " + session.getSessionIndex() + " " + e.getMessage());
        }
        transaction.setUsername(new String(form.getUsername()));
        transaction.setPassword(intPassword);
        if(transactionService.submitTransaction(transaction, session).length == 1){

            return "Improper credentials.";
        }
        session.setUserBean(databaseBeanFactory.createUserBean(session));
        sessionRepository.addSession(session);
        return "true";
    }

    public boolean logoff(int action, Session session) throws Exception{
        if(session.getUser() != null){
            User user = session.getUser();
            Map<String, String> entry = new HashMap<>();
            entry.put(new String("user"), user.getDetail(new String("index")));
            entry.put(new String("time"), new String(System.currentTimeMillis() + ""));
            entry.put(new String("action"), new String(action + ""));
            entry.put(new String("type"), session.getType());
            List<Map<String, String>> request = new ArrayList<>();
            DeviceCertificate dev = session.getDeviceCertificate();
            if(action != 3 && dev != null){
                deviceService.logoffDevice(dev);
            }else if (dev != null){
                deviceService.logDevice(dev, "Invalidated");
            }
            try{
                tableManagerService.createRows(request, new String("UserLogs"));
                return true;
            }catch (Exception e){
                debuggingService.throwDevException(new DevelopmentException("Failed to logoff session ID: " + session.getSessionIndex() + " " + e.getMessage() + " Class SessionService Method logoff"));
                debuggingService.nonFatalDebug("Failed to logoff session ID: " + session.getSessionIndex() + " " + e.getMessage());
                return false;
            }
        }else{
            return true;
        }
    }

    private boolean ResetPassword(String oldPassword, String newPassword, String passwordConfirm){
        return false;
    }

    //Action 5 = shutdown
    private boolean Shutdown(Session admin) throws Exception{
        try{
            List<Session> sessions = getActiveSessions(admin.getUser());
            for(Session session : sessions){
                LogOff(5, session);
            }
            return true;
        }catch (Exception e){
            try{
                debuggingService.throwDevException(new DevelopmentException("Failed to logoff all sessions during shutdown " + e.getMessage() + " Class SessionService Method Shutdown"));
                debuggingService.nonFatalDebug("Failed to logoff all sessions during shutdown " + e.getMessage());
                return false;
            }catch (Exception ex){
                return false;
            }
        }
    }

    private boolean LogOff(int action, Session session) throws Exception{
        Transaction transaction = new Transaction();
        if(session.getUser().getDetail(new String("username")) == null || session.getUser().getDetail(new String("password")) == null){
            return false;
        }
        try{
            transaction.setUsername(session.getUser().getDetail(new String("username")));
            transaction.setPassword(new BigInteger(session.getUser().getDetail(new String("password")).toString()));
            transaction.setOperation(new String("LOGOFF"));
            if(transactionService.submitTransaction(transaction, session).length == 1){
                return false;
            }
            if(session.getType().equals(new String("web"))){
                WebSession wsession = (WebSession)session;
                HttpSession http = ((WebSession) session).getHttpSession();
                if(http != null){
                    http.invalidate();
                    wsession.setHttpSession(null);
                }
                logoff(action, wsession);
            }else{
                logoff(action, session);
            }
            sessionRepository.removeSession(session);
            session = null;
            return true;
        }catch (Exception e){
            e.printStackTrace();
            debuggingService.throwDevException(new DevelopmentException("Failed to logoff. Session ID: " + session.getSessionIndex() + " " + e.getMessage() + " Class SessionService Method LogOff"));
            debuggingService.nonFatalDebug("Failed to logoff. Session ID: " + session.getSessionIndex() + " " + e.getMessage());
            return false;
        }
    }

    private boolean InvalidateSession(User user, String targetSessionUsername) throws Exception{
        if(user.getDetail(new String("admin")).equals(new String("true"))){
            Session target = sessionRepository.getSessionByUsername(targetSessionUsername);
            if(target == null){
                return false;
            }
            if(target.getType().equals(new String("web"))){
                WebSession session = (WebSession)target;
                HttpSession http = ((WebSession) target).getHttpSession();
                if(http != null){
                    http.invalidate();
                    session.setHttpSession(null);
                }
                logOff(3, session);
            }else{
                logOff(3, target);
            }
            sessionRepository.removeSession(target);
            target = null;
            return true;
        }else{
            return false;
        }
    }

}