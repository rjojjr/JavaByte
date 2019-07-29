package com.kirchnersolutions.database.sessions;

import com.kirchnersolutions.database.Servers.HTTP.HTTPController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.ui.Model;

import javax.servlet.http.*;
import java.util.Collection;
import java.util.Map;

@DependsOn({"sessionService"})
@Configuration
public class HttpSessionConfig {


    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionRepository sessionRepository;


    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                System.out.println("Session Created with session id+" + se.getSession().getId());
                se.getSession().setMaxInactiveInterval(30 * 60);
            }
            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
                System.out.println("Session Destroyed, Session id:" + se.getSession().getId());
                Session session = (Session)se.getSession().getAttribute("session");
                try {
                    sessionService.logoff(4, session);
                    sessionRepository.clean(session);
                    sessionRepository.TrimSessionList();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }
    @Bean
    public HttpSessionAttributeListener httpSessionAttributeListener() {
        return new HttpSessionAttributeListener() {
            @Override
            public void attributeAdded(HttpSessionBindingEvent se) {
                System.out.println("Attribute added the following information");
                System.out.println("Attribute Name:" + se.getName());
                System.out.println("Attribute Old Value:" + se.getValue());
            }
            @Override
            public void attributeRemoved(HttpSessionBindingEvent se) {
                System.out.println("Attribute removed the following information");
                System.out.println("Attribute Name:" + se.getName());
                System.out.println("Attribute Old Value:" + se.getValue());
            }
            @Override
            public void attributeReplaced(HttpSessionBindingEvent se) {
                System.out.println("Attribute Replaced following information");
                System.out.println("Attribute Name:" + se.getName());
                System.out.println("Attribute Old Value:" + se.getValue());
            }
        };
    }

    private static HttpSession cookie(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        if (request.getParameter("JSESSIONID") != null) {
            Cookie userCookie = new Cookie("JSESSIONID", request.getParameter("JSESSIONID"));
            response.addCookie(userCookie);
            return session;
        } else {
            String sessionId = session.getId();
            Cookie userCookie = new Cookie("JSESSIONID", sessionId);
            response.addCookie(userCookie);
            return session;
        }
    }
}