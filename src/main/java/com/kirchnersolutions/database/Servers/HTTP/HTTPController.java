package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.DeviceConfiguration;
import com.kirchnersolutions.database.Servers.HTTP.beans.IPBean;
import com.kirchnersolutions.database.Servers.HTTP.beans.LogonFormBean;
import com.kirchnersolutions.database.Servers.HTTP.beans.UserBean;
import com.kirchnersolutions.database.Servers.HTTP.beans.UserListBean;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionRepository;
import com.kirchnersolutions.database.sessions.WebSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

//@DependsOn({"httpService"})
@Controller
public class HTTPController {

    @Autowired
    private volatile HTTPService httpService;
    @Autowired
    private DeviceConfiguration deviceConfiguration;
    @Autowired
    private IPLogger ipLogger;
    @Autowired
    SessionRepository sessionRepository;

    @GetMapping("/")
    public String home(Model model, HttpServletResponse response, @RequestParam(required = false) String user) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        //ServletWebRequest servletWebRequest=new ServletWebRequest(request);
        //HttpServletResponse response=servletWebRequest.getResponse();
        HttpSession httpSession = cookie(request, response);
        if (user != null) {
            Session temp = httpService.getSessionByUsername(new String(user));
            if (temp == null) {
                ipLogger.log(request.getRemoteAddr(), "/home", "null");
                return "error";
            }
            WebSession session = (WebSession) temp;
            session.setHttpSession(httpSession);
            httpSession.setAttribute("session", session);
            UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
            if (userBean != null) {
                ipLogger.log(request.getRemoteAddr(), "/home", userBean.getUsername());
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            } else {
                ipLogger.log(request.getRemoteAddr(), "/home", "null");
                return "error";
            }
        } else {
            if (httpSession.getAttribute("session") == null) {
                ipLogger.log(request.getRemoteAddr(), "/home", "null");
                httpService.newSession(httpSession, request.getRemoteAddr());
                //model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
                model.addAttribute("form", new LogonFormBean());
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }
            UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
            if (userBean != null) {
                userBean.setMsg("");
                ipLogger.log(request.getRemoteAddr(), "/home", userBean.getUsername());
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            } else {
                //httpSession.setAttribute("session", new LogonFormBean());
                ipLogger.log(request.getRemoteAddr(), "/home", "null");
                model.addAttribute("form", new LogonFormBean());
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }
        }
    }

    @PostMapping("/logon")
    public String logon(Model model, @ModelAttribute LogonFormBean logonFormBean, HttpServletResponse respons) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        //ServletWebRequest servletWebRequest=new ServletWebRequest(request);
        //HttpServletResponse respons = servletWebRequest.getResponse();
        HttpSession httpSession = cookie(request, respons);
        ipLogger.log(request.getRemoteAddr(), "/logon", "null");
        try{

            if (httpSession.getAttribute("session") == null) {
                model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }

            UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
            if (userBean != null) {
                WebSession session = (WebSession) httpSession.getAttribute("session");
                if (session.getUser().getDetail(new String("Password Expired")) == null) {
                    model.addAttribute("user", userBean);
                    model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                    return "home";
                } else {
                    model.addAttribute("form", httpService.getResetPasswordForm(session));
                    model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                    return "resetPassword";
                }
            } else {
                //System.out.println(logonFormBean.getUsername() + " : " + logonFormBean.getPassword());
                String response = "";
                try {
                    response = httpService.submitLogonForm(logonFormBean, request.getRemoteAddr(), httpSession);
                    if (response.equals("true")) {
                        Session session = (Session) httpSession.getAttribute("session");
                        if (session.getUser().getDetail(new String("passwordExpired")) != null) {
                            model.addAttribute("form", httpService.getResetPasswordForm((WebSession) session));
                            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                            return "resetPassword";
                        }
                        userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
                        if (userBean != null) {
                            userBean.setMsg("");
                            model.addAttribute("user", userBean);
                            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                            return "home";
                        } else {
                            //System.out.println("Here");
                            return "error";
                        }
                    }
                } catch (Exception e) {
                    LogonFormBean form = new LogonFormBean();
                    form.setMsg("Invalid Credentials");
                    model.addAttribute("form", form);
                    model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                    return "logonForm";
                    //e.printStackTrace();
                    //System.out.println(e.getMessage());
                    //
                    // return "error";
                }

                LogonFormBean bean = new LogonFormBean();
                bean.setMsg(response);
                model.addAttribute("form", bean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }
        }catch (Exception ex){
            sessionRepository.TrimSessionList();
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @PostMapping("/password/reset/submit")
    public String resetPassword(Model model, @ModelAttribute LogonFormBean logonFormBean, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/password/reset/submit", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        try {
            userBean.setMsg("");
            WebSession session = (WebSession) httpSession.getAttribute("session");
            if (httpService.submitPasswordReset(logonFormBean, session)) {
                userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
                if (userBean != null) {
                    userBean.setMsg("");
                    ipLogger.log(request.getRemoteAddr(), "/password/reset/submit", userBean.getUsername());
                    model.addAttribute("user", userBean);
                    model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                    return "home";
                } else {
                    ipLogger.log(request.getRemoteAddr(), "/password/reset/submit", "null");
                    return "error";
                }
            }
        } catch (Exception e) {
            ipLogger.log(request.getRemoteAddr(), "/password/reset/submit", "null");
            return "error";
        }
        ipLogger.log(request.getRemoteAddr(), "/password/reset/submit", "null");
        model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
        model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
        return "logonForm";
    }

    @GetMapping("/logout")
    public String logoff(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/logout", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        try {
            Session session = (Session) httpSession.getAttribute("session");
            ipLogger.log(request.getRemoteAddr(), "/logout", session.getUser().getDetail("username"));
            if (httpService.logOff((Session) httpSession.getAttribute("session"))) {
                IPBean ip = new IPBean(request.getRemoteAddr());
                ip.setRefresh(true);
                model.addAttribute("form", new LogonFormBean());
                model.addAttribute("ip", ip);
                return "logonForm";
            } else {
                return "error";
            }
        } catch (Exception e) {
            ipLogger.log(request.getRemoteAddr(), "/logout", "null");
            e.printStackTrace();
            return "error";
        }
    }

    //User Console Functions

    @GetMapping("/users")
    public String userConsole(Model model, @RequestParam(required = false) String user, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        /*
        if (user != null) {
            Session temp = httpService.getSessionByUsername(new String(user));
            if (temp == null) {
                return "error";
            }
            WebSession session = (WebSession) temp;
            session.setHttpSession(httpSession);
            httpSession.setAttribute("session", session);

         */
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users", userBean.getUsername());
            if (userBean.isAdmin()) {
                model.addAttribute("user", userBean);
                model.addAttribute("list", httpService.getActiveUsers((WebSession) httpSession.getAttribute("session")));
                return "userConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        /*} else {
            if (httpSession.getAttribute("session") == null) {
                model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }
            UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
            if (userBean == null) {
                return "error";
            }
            if (userBean.isAdmin()) {
                model.addAttribute("user", userBean);
                return "usersConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                return "home";
            }
        }

         */

    }

    /*
    @GetMapping("/users/active")
    public String activeUserConsole(Model model, @RequestParam(required = false) String user) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = getSession();
        //if (user != null) {
            /*Session temp = httpService.getSessionByUsername(new String(user));
            if (temp == null) {
                return "error";
            }
            WebSession session = (WebSession) temp;
            session.setHttpSession(httpSession);
            httpSession.setAttribute("session", session);


        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            if (userBean.isAdmin()) {
                model.addAttribute("list", httpService.getActiveUsers((WebSession) httpSession.getAttribute("session")));
                model.addAttribute("user", userBean);
                return "usersActive";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            return "error";
        }
        //} else {
        /*
            if (httpSession.getAttribute("session") == null) {
                model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "logonForm";
            }
            UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
            if (userBean == null) {
                return "error";
            }
            if (userBean.isAdmin()) {
                model.addAttribute("user", userBean);
                return "usersConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                return "home";
            }
            }




    }
    */

    @GetMapping("/users/summary")
    public String userSummary(Model model, @RequestParam int id, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/summary", userBean.getUsername());
            if (userBean.isAdmin()) {
                UserListBean user = new UserListBean();
                user.setId(id + "");
                List<UserListBean> users = new ArrayList<>();
                try {
                    users = httpService.searchUsers(user);
                } catch (Exception e) {
                    e.printStackTrace();
                    userBean.setMsg("An error has occurred, contact system admin. ");
                }
                if (users != null && !users.isEmpty()) {
                    user = users.get(0);
                }
                model.addAttribute("UserListBean", user);
                model.addAttribute("user", userBean);
                return "usersSummary";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/summary", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @GetMapping("/users/edit")
    public String editUser(Model model, @RequestParam int id, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/edit", userBean.getUsername());
            if (userBean.isAdmin()) {
                UserListBean user = new UserListBean();
                user.setId(id + "");
                List<UserListBean> users = new ArrayList<>();
                try {
                    users = httpService.searchUsers(user);
                } catch (Exception e) {
                    userBean.setMsg("An Exception has occured\ncontact system admin\nException: " + e.getMessage());
                }
                if (users != null && !users.isEmpty()) {
                    user = users.get(0);
                }
                model.addAttribute("pw", new LogonFormBean());
                model.addAttribute("UserListBean", user);
                model.addAttribute("user", userBean);
                return "usersEdit";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/edit", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @PostMapping("/users/edit/submit")
    public String editUser(Model model, @ModelAttribute UserListBean userListBean, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        System.out.println(userListBean.getIndex());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/edit/submit", userBean.getUsername());
            if (userBean.isAdmin()) {
                try {
                    if (httpService.editUser(session, userListBean) != null) {
                        userBean.setMsg("User edit successful.");
                    } else {
                        userBean.setMsg("User edit unsuccessful.");
                    }
                } catch (Exception e) {
                    userBean.setMsg("An Exception has occured\nContact system admin\nException: " + e.getMessage());
                }
                model.addAttribute("UserListBean", userListBean);
                model.addAttribute("user", userBean);
                return "usersSummary";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/edit/submit", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @PostMapping("/users/edit/submitpassword")
    public String resetUserPW(Model model, @ModelAttribute UserListBean userListBean, @ModelAttribute LogonFormBean bean, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());

        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/edit/submitpassword", userBean.getUsername());
            if (userBean.isAdmin()) {
                try {
                    if (httpService.resetPassword(bean, userListBean.getId(), session) != false) {
                        userBean.setMsg("User password reset successful.");
                    } else {
                        userBean.setMsg("User password reset successful.");
                    }
                } catch (Exception e) {
                    userBean.setMsg("An Exception has occured\nContact system admin\nException: " + e.getMessage());
                }
                model.addAttribute("UserListBean", userListBean);
                model.addAttribute("user", userBean);
                return "usersSummary";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/edit/submitpassword", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @GetMapping("/users/create")
    public String userCreate(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        //System.out.println("session");
        //if(session == null){
        //   System.out.println("null session");
        //}
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        //System.out.println("userBean");
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/create/submit", userBean.getUsername());
            if (userBean.isAdmin()) {
                //System.out.println("usersCreate");
                model.addAttribute("UserListBean", new UserListBean());
                model.addAttribute("user", userBean);
                return "usersCreate";
            } else {
                //System.out.println("home");
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/create", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    @PostMapping("/users/create/submit")
    public String userCreateSubmit(Model model, @ModelAttribute UserListBean userListBean, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/create/submit", userBean.getUsername());
            if (userBean.isAdmin()) {
                UserListBean newUser = userListBean;
                try {
                    newUser = httpService.createUser(userListBean);
                } catch (Exception e) {
                    userBean.setMsg("An Exception has occured\ncontact system admin\nException: " + e.getMessage());
                }
                if (newUser != null) {
                    userBean.setMsg("User created succesfully");
                    model.addAttribute("UserListBean", newUser);
                    model.addAttribute("user", userBean);
                    return "usersSummary";
                } else {
                    userListBean.setPassword("");
                    userBean.setMsg("User not created succesfully");
                    model.addAttribute("UserListBean", userListBean);
                    model.addAttribute("user", userBean);
                    return "usersCreate";
                }
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/create/submit", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    /*
    @GetMapping("/users/message")
    public String sendUserMessage(Model model) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = getSession();
        WebSession session = (WebSession) httpSession.getAttribute("session");
        session.setHttpSession(httpSession);
        httpSession.setAttribute("session", session);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            if (userBean.isAdmin()) {
                model.addAttribute("list", httpService.getActiveUsers(session));
                model.addAttribute("user", userBean);
                return "usersMessage";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }



    @GetMapping("/users/users")
    public String usersUsers(Model model) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = getSession();
        WebSession session = (WebSession) httpSession.getAttribute("session");
        session.setHttpSession(httpSession);
        httpSession.setAttribute("session", session);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setResultPage(false);
            if (userBean.isAdmin()) {
                List<UserListBean> list = new ArrayList<UserListBean>();
                model.addAttribute("bean", new UserListBean());
                model.addAttribute("user", userBean);
                model.addAttribute("list", list);
                return "usersUsers";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    @PostMapping("/users/users/submit")
    public String usersSubmit(Model model, @ModelAttribute UserListBean userListBean) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = getSession();
        WebSession session = (WebSession) httpSession.getAttribute("session");
        session.setHttpSession(httpSession);
        httpSession.setAttribute("session", session);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            if (userBean.isAdmin()) {
                List<UserListBean> list = new ArrayList<>();
                List<UserListBean> matches = new ArrayList<>();
                try {
                    matches = httpService.searchUsers(userListBean);
                } catch (Exception e) {
                    e.printStackTrace();
                    userBean.setMsg("An Exception has occured\ncontact system admin\nException: " + e.getMessage());
                }
                if (matches != null && matches.size() > 0) {
                    list = matches;
                }

                userBean.setResultPage(true);
                model.addAttribute("list", list);
                model.addAttribute("bean", userListBean);
                model.addAttribute("user", userBean);
                return "usersUsers";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    @GetMapping("/users/message/send")
    public String sendMessage(Model model, @RequestParam String msg) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = getSession();
        WebSession session = (WebSession) httpSession.getAttribute("session");
        session.setHttpSession(httpSession);
        httpSession.setAttribute("session", session);
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            if (userBean.isAdmin()) {
                model.addAttribute("list", httpService.getActiveUsers(session));
                model.addAttribute("user", userBean);
                return "usersMessage";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }


     */
    @GetMapping("/users/kick")
    public String kickUser(Model model, @RequestParam int id, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session temp = httpService.getSessionByID(new String(id + ""));
        if (temp == null) {
            return "error";
        }
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        try {
            if (httpService.kickUser(session, temp)) {
                userBean.setMsg("User kicked.");
            } else {
                userBean.setMsg("User not kicked.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            userBean.setMsg("User not kicked. " + e.getMessage());
        }
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/users/kick", userBean.getUsername());
            if (userBean.isAdmin()) {
                model.addAttribute("list", httpService.getActiveUsers(session));
                model.addAttribute("user", userBean);
                return "userConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/users/kick", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    //Transactions
    @GetMapping("/transactions")
    public String transConsole(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/transactions", userBean.getUsername());
            if (userBean.isAdmin()) {
                model.addAttribute("user", userBean);
                return "transactionConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/transactions", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    //maint
    @GetMapping("/maintenance")
    public String maintConsole(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        Session session = (Session) httpSession.getAttribute("session");
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/maintenance", userBean.getUsername());
            if (userBean.isAdmin()) {
                model.addAttribute("user", userBean);
                return "maintConsole";
            } else {
                userBean.setMsg("You do not have privilege to this page.");
                model.addAttribute("user", userBean);
                model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
                return "home";
            }
        } else {
            ipLogger.log(request.getRemoteAddr(), "/maintenance", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }

    }

    //Tables
    @GetMapping("/tables")
    public String tables(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/tables", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/tables", userBean.getUsername());
            model.addAttribute("user", userBean);
            model.addAttribute("tables", httpService.getTableList());
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "tableConsole";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/tables", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //Loggs
    @GetMapping("/logs")
    public String logs(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/logs", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null && userBean.isAdmin()) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/logs", userBean.getUsername());
            model.addAttribute("user", userBean);
            model.addAttribute("list", httpService.getLogs(userBean));
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logConsole";
        } else if (userBean != null && !userBean.isAdmin()) {
            userBean.setMsg("You do not have privilege to this page.");
            ipLogger.log(request.getRemoteAddr(), "/home", userBean.getUsername());
            model.addAttribute("user", userBean);
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "home";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/logs", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //Devbice
    @GetMapping("/devices")
    public String deviceConsole(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/devices", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/devices", userBean.getUsername());
            model.addAttribute("user", userBean);
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "construction";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/devices", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //User Options
    @GetMapping("/user/options")
    public String userOptions(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/user/options", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());

        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/user/options", userBean.getUsername());
            model.addAttribute("user", userBean);
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "construction";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/user/options", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //Traffic

    @GetMapping("/traffic")
    public String traffic(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/traffic", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());

        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/traffic", userBean.getUsername());
            model.addAttribute("user", userBean);
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "trafficConsole";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/traffic", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //Data Management
    @GetMapping("/data")
    public String dataManagement(Model model, HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if (httpSession.getAttribute("session") == null) {
            ipLogger.log(request.getRemoteAddr(), "/data", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
        UserBean userBean = httpService.getUserBean(httpSession, request.getRemoteAddr());
        if (userBean != null) {
            userBean.setMsg("");
            ipLogger.log(request.getRemoteAddr(), "/data", userBean.getUsername());
            model.addAttribute("user", userBean);
            model.addAttribute("tables", httpService.getTableList());
            // model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "dataConsole";
        } else {
            ipLogger.log(request.getRemoteAddr(), "/data", "null");
            model.addAttribute("form", httpService.newSession(httpSession, request.getRemoteAddr()));
            model.addAttribute("ip", new IPBean(request.getRemoteAddr()));
            return "logonForm";
        }
    }

    //Errors

    //Device has no certificate
    @GetMapping("/notallowed")
    public String deviceNotAllowed(Model model) {
        if (deviceConfiguration.isOnlyRegDevices()) {
            return "notCertified";
        } else {
            return "error";
        }
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    private static HttpSession cookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null && request.getParameter("JSESSIONID") != null) {
            Cookie userCookie = new Cookie("JSESSIONID", request.getParameter("JSESSIONID"));
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        } else if(session != null){
            String sessionId = session.getId();
            Cookie userCookie = new Cookie("JSESSIONID", sessionId);
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        }else{
            session = getSession();
            String sessionId = session.getId();
            Cookie userCookie = new Cookie("JSESSIONID", sessionId);
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        }
    }

}

