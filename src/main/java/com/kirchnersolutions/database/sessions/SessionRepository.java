package com.kirchnersolutions.database.sessions;

import com.kirchnersolutions.database.objects.User;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScope
@Repository
public class SessionRepository {

    private List<Session> sessionList = Collections.synchronizedList(new ArrayList<>());
    private AtomicInteger index = new AtomicInteger(0);

    /**
     * Returns session assciated with username.
     * Returns null if no exception exists.
     *
     * @param username
     * @return
     */
    Session getSessionByUsername(String username) {
        return GetSessionByUsername(username);
    }

    List<Session> getActiveSessions(User user) {
        if (user.getDetail(new String("admin")).equals(new String("false"))) {
            return null;
        }
        return new ArrayList<>(sessionList);
    }

    /**
     * Add session.
     *
     * @param session
     */
    void addSession(Session session) {
        AddSession(session);
        //System.out.println(sessionList.size());
    }

    /**
     * Remove session.
     *
     * @param session
     */
    void removeSession(Session session) {
        if(!sessionList.isEmpty()){
            RemoveSession(session);
        }
    }

    /**
     * Sets user's stomp ID.
     * Returns false if user has no web session.
     *
     * @param username
     * @param stompID
     * @return
     */
    boolean setStompID(String username, String stompID) {
        return SetStompID(username, stompID);
    }

    /**
     * Gets user's stomp ID.
     * Returns null if user has no web session.
     *
     * @param username
     * @return
     */
    String getStompID(String username) {
        return GetStompID(username);
    }

    /**
     * Returns list of active sessions matching request parameters.
     * @param request
     * @return
     */
    public List<Session> getSessions(Map<String, String> request){
        return GetSessions(request);
    }

    /**
     * Return user sessioj by id.
     *
     * @param id
     * @return
     */
    Session getSessionByID(String id) {
        return GetSessionByID(id);
    }

    private void RemoveSession(Session session) {
        int ind = session.getSessionIndex();
        if (ind != -1 && ind <= sessionList.size()) {
            sessionList.remove(ind);
            clean(session);
            TrimSessionList();
        }else{
            clean(session);
        }
    }

    private boolean SetStompID(String username, String stompID) {
        WebSession session = (WebSession) GetWebSessionByUsername(username);
        if (session != null) {
            session.setStompID(stompID);
            return true;
        }
        return false;
    }

    private String GetStompID(String username) {
        WebSession session = (WebSession) GetWebSessionByUsername(username);
        if (session != null) {
            return session.getStompID();
        }
        return null;
    }

    private Session GetWebSessionByUsername(String username) {
        for (Session session : getSessions()) {
            User user = session.getUser();
            if (user != null) {
                if (user.getDetail(new String("username")).equals(username) && session.getType().equals(new String("web"))) {
                    return session;
                }
            }
        }
        return null;
    }

    private Session GetSessionByID(String id) {
        //System.out.println("Seesion loop");
        List<Session> sess = getSessions();
        for (Session session : sess) {
            User user = session.getUser();
            //System.out.println("Seesion loop");
            if (user != null) {
                if (user.getDetail(new String("id")).equals(id)) {
                    return session;
                }
            }
        }
        return null;
    }

    void shutdown(){
        List<Session> sessions = getSessions();
    }

    private List<Session> getSessions() {
        return new ArrayList<>(sessionList);
    }

    private synchronized void setSessionList(List<Session> list) {
        sessionList = Collections.synchronizedList(new ArrayList<>(list));
    }

    @Scheduled(fixedDelay = 1000 * 15)
    public synchronized void TrimSessionList() {
        if(sessionList.isEmpty()){
            List<Session> newList = new ArrayList<>();
            index = new AtomicInteger(newList.size());
            setSessionList(newList);
        }else{
            List<Session> newList = new ArrayList<>();
            int count = 0;
            for (Session session : getSessions()) {
                if (session != null) {
                    session.setIndex(count);
                    newList.add(session);
                    count++;
                }
            }
            index = new AtomicInteger(newList.size());
            setSessionList(newList);
        }

    }

    private void AddSession(Session session) {
        session.setIndex(index.intValue());
        clean(session);
        List<Session> sessions = getSessions();
        sessions.add(index.getAndIncrement(), session);
        setSessionList(sessions);
    }

    public synchronized void clean(Session session){
        List<Session> temp = new ArrayList<>(sessionList);
        for(Session tsession : temp){
            if(tsession.getUser() != null && tsession.getUser().getDetail("username").equals(session.getUser().getDetail("username"))){
                temp.remove(tsession);
                break;
            }
        }
        setSessionList(temp);
    }

    private List<Session> GetSessions(Map<String, String> request) {
        List<Session> sessions = new ArrayList<>();
        for (Session session : getSessions()) {
            boolean found = true;
            for (String key : request.keySet()) {
                if(!session.getDetail(key).equals(request.get(key))){
                    found = false;
                    break;
                }
            }
            if(found){
                sessions.add(session);
            }
        }
        return sessions;
    }

    private Session GetSessionByUsername(String username) {
        for (Session session : getSessions()) {
            User user = session.getUser();
            if (user != null) {
                if (user.getDetail(new String("username")).equals(username)) {
                    return session;
                }
            }
        }
        return null;
    }

}