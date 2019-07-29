package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Servers.HTTP.beans.*;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.core.tables.UserService;
import com.kirchnersolutions.database.dev.DebugLogRepository;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.*;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.database.sessions.*;
import com.kirchnersolutions.utilities.CryptTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


@Service
@DependsOn({"sessionService", "deviceService"})
public class HTTPService {

    @Autowired
    private DatabaseBeanFactory databaseBeanFactory;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private DebugLogRepository debugLogRepository;
    @Autowired
    private DebuggingService debuggingService;

    public HTTPService(){

    }

    LogonFormBean newLogonForm(HttpSession httpSession){
        return databaseBeanFactory.newLogonFormBean(httpSession);
    }

    String submitLogonForm(LogonFormBean form, String ip, HttpSession httpSession) throws Exception{
        try{
            WebSession session = (WebSession)httpSession.getAttribute("session");
            if(session == null){

            }

            session.setIp(ip);
            return sessionService.logon(form, session);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    boolean logOff(Session session) throws Exception{
        try{
            return sessionService.logOff(2, session);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    LogonFormBean getResetPasswordForm(WebSession session){
        return databaseBeanFactory.newPasswordResetFormBean(session);
    }

    UserListBean getUserListBean(Session session){
        return GetUserListBean(session);
    }

    UserBean getUserBean(HttpSession httpSession, String ip){
        if(httpSession.getAttribute("session") == null){
            //System.out.println("null");
            return null;
        }
        WebSession session = (WebSession)httpSession.getAttribute("session");
        session.setIp(ip);
        return session.getUserBean();
    }

    UserListBean editUser(Session admin, UserListBean subject) throws Exception{
        return EditUser(admin, subject);
    }

    List<UserListBean> searchUsers(UserListBean bean) throws Exception{
        return SearchUsers(bean);
    }

    String checkHttpStatus(String username){
        return CheckHttpStatus(username);
    }

    LogonFormBean newSession(HttpSession httpSession, String ip){
        return newLogonForm(httpSession);
    }

    UserListBean createUser(UserListBean bean) throws Exception{
        return CreateUser(bean);
    }

    List<String> getTableList(){
        return GetTableList();
    }

    boolean resetPassword(LogonFormBean bean, String userID, Session admin) throws Exception{
        return SubmitPasswordReset(bean, userID, admin);
    }

    boolean submitPasswordReset(LogonFormBean form, WebSession session) throws Exception{
        return SubmitPasswordReset(form, session);
    }

    Session getSessionByUsername(String username){
        return GetSessionByUsername(username);
    }

    List<UserListBean> getActiveUsers(Session session){
       return GetActiveUsers(session);
    }

    Session getSessionByID(String id){
        return sessionService.getSessionByID(id);
    }

    boolean kickUser(Session admin, Session kick) throws Exception{
        return sessionService.invalidateSession(admin.getUser(), kick.getUser().getDetail(new String("username")));
    }

    private List<UserListBean> GetActiveUsers(Session session){
        List<Session> sessions = sessionService.getActiveSessions(session.getUser());
        if(sessions == null){
            return null;
        }
        return generateUserListBeans(sessions);
    }

    private UserListBean EditUser(Session admin, UserListBean subject) throws Exception{
        return userService.editUser(admin, subject);
    }

    private Session GetSessionByUsername(String username){
        return sessionService.getSessionByUsername(username);
    }

    private String CheckHttpStatus(String username){
        Session temp = sessionService.getSessionByUsername(username);
        if(temp == null){
            return "logged off";
        }
        WebSession session = (WebSession)temp;
        if(session.getHttpSession() == null){
            return "invalid";
        }
        return "valid";
    }

    private boolean SubmitPasswordReset(LogonFormBean form, WebSession session) throws Exception{
        boolean success = false;
        try{
            success = userService.resetPassword(session, form.getPassword(), form.getNewpassword(), form.getPasswordConfirm());
        }catch (Exception e){
            debuggingService.nonFatalDebug("Failed to reset user " + session.getUser().getDetail(new String("username")) + " password. " + e.getMessage());
        }
        return success;
    }

    private List<String> GetTableList(){
        return tableManagerService.getTableNames();
    }

    private boolean SubmitPasswordReset(LogonFormBean form, String id, Session admin) throws Exception{
        boolean success = false;
        if(!form.getNewpassword().equals(form.getPasswordConfirm())){
            return false;
        }
        try{
            success = userService.adminResetPassword(admin, form.getNewpassword(), id);
        }catch (Exception e){
            debuggingService.nonFatalDebug("Failed to reset user id: " + id + " password. " + e.getMessage());
        }
        return success;
    }

    private List<UserListBean> SearchUsers(UserListBean bean) throws Exception{
        Map<String, String> activeRequest = new HashMap<>();
        Map<String, String> dbRequest = new HashMap<>();
        if(!bean.getUsername().equals("")){
            //udbSystem.out.println("Username search: " + bean.getUsername());
            activeRequest.put(new String("username"), new String(bean.getUsername()));
            dbRequest.put(new String("username"), new String(bean.getUsername()));
        }
        if(!bean.getFirstname().equals("")){
            //udbSystem.out.println("Firstname search: " + bean.getFirstname());
            activeRequest.put(new String("firstname"), new String(bean.getFirstname()));
            dbRequest.put(new String("firstname"), new String(bean.getFirstname()));
        }
        if(!bean.getLastname().equals("")){
            //udbSystem.out.println("Lastname search: " + bean.getLastname());
            activeRequest.put(new String("lastname"), new String(bean.getLastname()));
            dbRequest.put(new String("lastname"), new String(bean.getLastname()));
        }
        if(!bean.getId().equals("")){
            //udbSystem.out.println("ID search: " + bean.getId());
            activeRequest.put(new String("id"), new String(bean.getId()));
            dbRequest.put(new String("id"), new String(bean.getId()));
        }
        if(!bean.getAdmin().equals("")){
            //udbSystem.out.println("Admin search: " + bean.getAdmin());
            activeRequest.put(new String("admin"), new String(bean.getAdmin()));
            dbRequest.put(new String("admin"), new String(bean.getAdmin()));
        }
        if(!bean.getIndex().equals("")){
            //udbSystem.out.println("Index search: " + bean.getIndex());
            activeRequest.put(new String("index"), new String(bean.getIndex()));
            dbRequest.put(new String("index"), new String(bean.getIndex()));
        }
        if(!bean.getIp().equals("")){
            //udbSystem.out.println("IP search: " + bean.getIp());
            activeRequest.put(new String("ip"), new String(bean.getIp()));
            dbRequest = null;
        }
        if(!bean.getSessiontype().equals("")){
            //udbSystem.out.println("Session Type search: " + bean.getSessiontype());
            activeRequest.put(new String("type"), new String(bean.getSessiontype()));
            dbRequest = null;
        }
        if(!bean.getDevice().equals("")){
            //udbSystem.out.println("Device search: " + bean.getDevice());
            activeRequest.put(new String("device"), new String(bean.getDevice()));
            dbRequest = null;
        }
        //dbRequest = null;
        List<Session> sessions = new ArrayList<>();
        List<User> users = new ArrayList<>();
        Future<List<Session>> sessionFuture = threadPoolTaskExecutor.submit(new SearchSession(activeRequest));
        if(dbRequest != null){
            Future<List<User>> dbFuture = threadPoolTaskExecutor.submit(new SearchUserDB(dbRequest));
            sessions = sessionFuture.get();
            users = dbFuture.get();
        }else{
            sessions = sessionFuture.get();
            users = null;
        }
        List<UserListBean> combine = GetSearchResults(sessions, users);
        List<UserListBean> result = new ArrayList<UserListBean>();
        if(combine == null){
            return new ArrayList<UserListBean>();
        }
        boolean allNull = true;
        for (UserListBean listbean : combine){
            if(listbean != null){
                result.add(listbean);
                allNull = false;
            }
        }
        if(allNull){
            return new ArrayList<UserListBean>();
        }
        return result;
    }

    private List<UserListBean> GetSearchResults(List<Session> sessions, List<User> users){
        List<User> unique = new ArrayList<>();
        if(sessions != null && users != null){
            for(User user : users){
                boolean repeat = false;
                if(user != null){
                    for(Session session : sessions){
                        if(user.getDetail(new String("index")).equals(session.getUserIndex())){
                            repeat = true;
                            break;
                        }
                    }
                    if(!repeat){
                        unique.add(user);
                    }
                }else{

                }
            }
            List<UserListBean> results = new ArrayList<>();
            for(Session session : sessions){
                results.add(GetUserListBean(session));
            }
            for(User session : unique){
                results.add(GetUserListBean(session));
            }
            return results;
        }else if(users == null && sessions == null){
            return null;
        }else if(users == null){
            List<UserListBean> results = new ArrayList<>();
            for(Session session : sessions){
                results.add(GetUserListBean(session));
            }
            return results;
        }
        else if(sessions == null){
            List<UserListBean> results = new ArrayList<>();
            for(User session : users){
                results.add(GetUserListBean(session));
            }
            return results;
        }
        return null;
    }

    private UserListBean CreateUser(UserListBean bean) throws Exception{
        Map<String, String> newUser = new HashMap<>();
        if(!bean.getUsername().equals("")){
            newUser.put(new String("username"), new String(bean.getUsername()));
        }else{
            System.out.println("Username Problem");
            return null;
        }
        if(!bean.getFirstname().equals("")){
            newUser.put(new String("firstname"), new String(bean.getFirstname()));
        }else{
            System.out.println("Firstname Problem");
            return null;
        }
        if(!bean.getLastname().equals("")){
            newUser.put(new String("lastname"), new String(bean.getLastname()));
        }else{
            System.out.println("Lastname Problem");
            return null;
        }
        if(!bean.getId().equals("")){
            newUser.put(new String("id"), bean.getId());
        }else{
            System.out.println("ID Problem");
            return null;
        }
        if(!bean.getPassword().equals("")){
            try{
                newUser.put(new String("password"), new String(new BigInteger(CryptTools.getSHA256(bean.getPassword())).toString()));
                bean.setPassword("");
            }catch (Exception e){
                bean.setPassword("");
                debuggingService.nonFatalDebug("Cannot create user " + bean.getUsername() + " failed to proccess pasword. " + e.getMessage());
                return null;
            }
        }else{
            return null;
        }
        if(!bean.getAdmin().equals("")){
            newUser.put(new String("admin"), new String(bean.getAdmin()));
        }else{
            System.out.println("Role Problem");
            return null;
        }
        if(userService.createUser(newUser)){
            return bean;
        }else {
            System.out.println("userService Problem");
            return null;
        }
    }

    List<LogBean> getLogs(UserBean session){
        if(session.isAdmin()){
            return debugLogRepository.getBeans();
        }else{
            return null;
        }
    }

    private UserListBean GetUserListBean(Session session){
        User user = session.getUser();
        UserListBean bean = new UserListBean();
        bean.setAdmin(user.getDetail(new String("admin")).toString());
        bean.setUsername(user.getDetail(new String("username")).toString());
        bean.setFirstname(user.getDetail(new String("firstname")).toString());
        bean.setLastname(user.getDetail(new String("lastname")).toString());
        bean.setId(user.getDetail(new String("id")).toString());
        bean.setIndex(user.getDetail(new String("index")).toString());
        bean.setIp(session.getIp());
        bean.setSessiontype(session.getType().toString());
        bean.setDevice("null");
        //bean.setDevice(deviceService.getActiveDeviceFromUsername(user.getDetail(new String("username"))).getType().toString());
        return bean;
    }

    private UserListBean GetUserListBean(User user){
        UserListBean bean = new UserListBean();
        bean.setAdmin(user.getDetail(new String("admin")).toString());
        bean.setUsername(user.getDetail(new String("username")).toString());
        bean.setFirstname(user.getDetail(new String("firstname")).toString());
        bean.setLastname(user.getDetail(new String("lastname")).toString());
        bean.setId(user.getDetail(new String("id")).toString());
        bean.setIndex(user.getDetail(new String("index")).toString());
        bean.setIp("null");
        bean.setSessiontype("null");
        bean.setDevice("null");
        return bean;
    }

    private List<UserListBean> generateUserListBeans(List<Session> sessions){
        List<UserListBean> result = new ArrayList<>();
        for(Session session : sessions){
            User user = session.getUser();
            UserListBean bean = new UserListBean();
            bean.setAdmin(user.getDetail(new String("admin")).toString());
            bean.setUsername(user.getDetail(new String("username")).toString());
            bean.setFirstname(user.getDetail(new String("firstname")).toString());
            bean.setLastname(user.getDetail(new String("lastname")).toString());
            bean.setId(user.getDetail(new String("id")).toString());
            bean.setIndex(user.getDetail(new String("index")).toString());
            bean.setIp(session.getIp());
            bean.setSessiontype(session.getType().toString());
            bean.setDevice(session.getDeviceType());
            //bean.setDevice(deviceService.getActiveDeviceFromUsername(user.getDetail(new String("username"))).getType().toString());
            result.add(bean);
        }
        return result;
    }

    private class SearchUserDB implements Callable<List<User>>{

        private volatile Map<String, String> request;

        public SearchUserDB(Map<String, String> request){
            this.request = request;
        }

        @Override
        public List<User> call() throws Exception {
            Thread.currentThread().setName("HTTPService:SearchUserDB:");
            try{
                return userService.searchUserDB(request);
            }catch (Exception e){
                e.printStackTrace();
                debuggingService.throwDevException(new DevelopmentException("Exception in thread " + Thread.currentThread().getName() + e.getMessage() + " Class HTTPService Method SearchUserDB"));
                debuggingService.nonFatalDebug("Exception in thread " + Thread.currentThread().getName() + e.getMessage());
            }
            return null;
        }
    }

    private class SearchSession implements Callable<List<Session>>{

        private volatile Map<String, String> request;

        public SearchSession(Map<String, String> request){
            this.request = request;
        }

        @Override
        public List<Session> call() throws Exception {
            Thread.currentThread().setName("HTTPService:SearchSession:");
            try{
                return sessionRepository.getSessions(request);
            }catch (Exception e){
                debuggingService.throwDevException(new DevelopmentException("Exception in thread " + Thread.currentThread().getName() + e.getMessage() + " Class HTTPService Method SearchSession"));
                debuggingService.nonFatalDebug("Exception in thread " + Thread.currentThread().getName() + e.getMessage());
            }
            return null;
        }
    }
}