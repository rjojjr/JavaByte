package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.ConfigurationService;
import com.kirchnersolutions.database.Servers.HTTP.beans.*;
import com.kirchnersolutions.database.core.tables.TransactionService;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.WebSession;
import com.kirchnersolutions.utilities.CalenderConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@DependsOn("maintenanceService")
public class GeneralStompService {


    @Autowired
    private volatile SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private volatile SessionService sessionService;
    @Autowired
    private volatile SystemStatsService systemStatsService;
    @Autowired
    private volatile SystemBench systemBench;
    @Autowired
    private volatile HTTPService httpService;
    @Autowired
    private volatile MaintenanceService maintenanceService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private IPLogger ipLogger;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private TransactionService transactionService;

    List<String[]> pings = new ArrayList<>();

    void stopCPUBench() {
        systemBench.stop();
    }

    //@Scheduled(fixedDelay=1500)
    public void sendMsgs(SystemStompMessage systemStompMessage) {
        String queue = "/queue/specific-user";
        this.simpMessagingTemplate.convertAndSendToUser(systemStompMessage.getIndex(), "/queue/notify", systemStompMessage.getPage(), createHeaders(systemStompMessage.getIndex()));
        //this.simpMessagingTemplate.convertAndSendToUser(systemStompMessage.getIndex(), queue, systemStompMessage.getPage(), createHeaders(systemStompMessage.getIndex()));
        debuggingService.stompDebug("@Stomp Send message to stomp ID: " + systemStompMessage.getIndex() + " Message: " + systemStompMessage.getPage());
    }

    public void sendUserMsgs(UserMessageBean bean) {
        List<String> users = bean.getUserNames();
        for (String user : users) {
            Session session = sessionService.getSessionByUsername(new String(user));
            if (session != null && session.getType().equals(new String("web"))) {
                WebSession webSession = (WebSession) session;
                SystemStompMessage msg = new SystemStompMessage();
                msg.setIndex(webSession.getStompID());

                String out = "3%" + bean.getFrom() + "%" + bean.getMessage();
                debuggingService.stompDebug("@Stomp Sent Message: " + bean.getMessage() + " From: " + bean.getFrom());
                msg.setPage(out);
                sendMsgs(msg);
            } else {

            }

        }
    }

    void getChangelog(String username, String sessionID){
        try{
            String temp = "change%" + documentService.getChangelog();
            this.simpMessagingTemplate.convertAndSendToUser(sessionID, "/queue/notify", temp, createHeaders(sessionID));
            debuggingService.stompDebug("@Stomp User " + username + " Changelog request successful");
        }catch (Exception e){
            debuggingService.stompDebug("@Stomp User " + username + " Changelog request unsuccessful");
            this.simpMessagingTemplate.convertAndSendToUser(sessionID, "/queue/notify", "change%An exception has occurred.", createHeaders(sessionID));
        }
    }

    void getDoc(String username, String sessionID){
        try{
            String temp = "doc%" + documentService.getDocs();
            this.simpMessagingTemplate.convertAndSendToUser(sessionID, "/queue/notify", temp, createHeaders(sessionID));
            debuggingService.stompDebug("@Stomp User " + username + " Documentation request successful");
        }catch (Exception e){
            debuggingService.stompDebug("@Stomp User " + username + " Documentation request unsuccessful");
            this.simpMessagingTemplate.convertAndSendToUser(sessionID, "/queue/notify", "doc%An exception has occurred.", createHeaders(sessionID));
        }
    }

    void pingInit(String stompID) {
        long t = System.currentTimeMillis();
        String[] ping = new String[2];
        ping[0] = stompID;
        ping[1] = t + "";
        //debuggingService.stompDebug("@Stomp Ping request by " + stompID + " at " + CalenderConverter.getMonthDayYearHourMinuteSecond(t, "/", ":"));
        pings.add(ping);
    }

    long pingResult(String stompID) {
        long t = System.currentTimeMillis(), ping = (long) 0;
        String[] request = null;
        List<String[]> pingComp = new ArrayList<>(pings);
        for (String[] req : pingComp) {
            if (req[0].equals(stompID)) {
                long start = Long.parseLong(req[1]);
                ping = t - start;
                pingComp.remove(req);
                pingUpdate(pingComp);
                //debuggingService.stompDebug("@Stomp Ping request by " + stompID + " completed at " + CalenderConverter.getMonthDayYearHourMinuteSecond(t, "/", ":") + " in " + ping + " milliseconds");
                return ping;
            }
        }
        debuggingService.stompDebug("@Stomp Ping request by " + stompID + " failed at " + CalenderConverter.getMonthDayYearHourMinuteSecond(t, "/", ":") + " no initial request found");
        return ping;
    }

    void getVars(StompTableRequest request){
        String[] vars = configurationService.getVarsNames(request.getQuery());
        String out = "";
        boolean first = true;
        for (String var : vars){
            if(first){
                out = "var%" + var;
                first = false;
            }else{
                out = out + "," + var;
            }
        }
        this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", out, createHeaders(request.getSessionid()));
    }

    void setVars(StompTableRequest request) throws Exception{
        Session session = sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            String type = request.getTablename();
            String[] vars = request.getQuery().split(",");
            int count = 0;
            for(String var : vars){
                String[] temp = var.split(":");
                vars[count] = temp[1];
                count++;
            }
            try{
                configurationService.updateConfig(type, vars);
                request.setQuery(type);
                getVars(request);
            }catch (Exception e){
                this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "3%System%There was a problem with your request.", createHeaders(request.getSessionid()));
                debuggingService.stompDebug("@Stomp Set variable request by: " + request.getUsername() + " failed due to exception");
                debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
                debuggingService.nonFatalDebug(e.getMessage());
            }
        }else {
            debuggingService.stompDebug("@Stomp Set variable request by: " + request.getUsername() + " failed due to invalid credentials");
            this.simpMessagingTemplate.convertAndSendToUser(request.getSessionid(), "/queue/notify", "deny%System%You do not have the credentials to use this function.", createHeaders(request.getSessionid()));
        }
    }

    void benchCPU(String stompID) {
        long stime = System.currentTimeMillis();
        String score = "";
        debuggingService.stompDebug("@Stomp CPU Bench request by " + stompID + " at " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":"));
        try {
            score = systemBench.cpuBench(systemStatsService.getCPUThreadCount()).toString();
        } catch (Exception e) {
            score = e.getMessage();
            debuggingService.stompDebug("@Stomp CPU Bench request by " + stompID + " failed at " + CalenderConverter.getMonthDayYearHourMinuteSecond(System.currentTimeMillis(), "/", ":") + " " + e.getMessage());
        }
        stime = System.currentTimeMillis() - stime;
        stime/= 1000;
        this.simpMessagingTemplate.convertAndSend("/maint/bench/score", "" + score + "\n" + "Run in " + stime + " seconds");
    }

    void reboot(String username, Session admin){
        Session session = sessionService.getSessionByUsername(username);
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            debuggingService.stompDebug("@Stomp Application reboot called by " + username);
            maintenanceService.restartApplication(admin);
        }else {
            debuggingService.stompDebug("@Stomp Application reboot request by: " + username + " failed due to invalid credentials");

        }

    }

    void shutdown(String username, Session session){
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            debuggingService.stompDebug("@Stomp Application shutdown called by " + username);
            maintenanceService.shutdown(session);
        }else {
            debuggingService.stompDebug("@Stomp Application shutdown request by: " + username + " failed due to invalid credentials");
        }

    }

    void searchUsers(StompUserSearch search) throws Exception{

        UserListBean bean = new UserListBean();
        for (String par : search.getRequest().split(",")) {
            String[] vals = par.split(":");
            if (vals[0].equals("username")) {
                bean.setUsername(vals[1]);
            }
            if (vals[0].equals("firstname")) {
                bean.setFirstname(vals[1]);
            }
            if (vals[0].equals("lastname")) {
                bean.setLastname(vals[1]);
            }
            if (vals[0].equals("userid")) {
                bean.setId(vals[1]);
            }
            if (vals[0].equals("admin")) {
                bean.setAdmin(vals[1]);
            }
            if (vals[0].equals("index")) {
                bean.setIndex(vals[1]);
            }
            if (vals[0].equals("ip")) {
                bean.setIp(vals[1]);
            }
            if (vals[0].equals("session")) {
                bean.setSessiontype(vals[1]);
            }
            if (vals[0].equals("device")) {
                bean.setDevice(vals[1]);
            }
        }
        List<UserListBean> matches = httpService.searchUsers(bean);
        String result = "";
        boolean first = true;
        int count = 0;
        for(UserListBean ubean : matches){
            if(first){
                result = "10%" + ubean.toString();
                first = false;
            }else {
                result+= "," + ubean.toString();
            }
        }
        debuggingService.stompDebug("@Stomp User search called by " + search.getUsername() + " completed with " + count + " results");
        this.simpMessagingTemplate.convertAndSendToUser(search.getStompID(), "/queue/notify", result, createHeaders(search.getStompID()));
    }

    void searchTraffic(StompUserSearch search) throws Exception{
        String ip = "", username = "", page = "", sdate = "", edate = "";
        for (String par : search.getRequest().split(",")) {
            String[] vals = par.split(":");
            if (vals[0].equals("username")) {
                username = (vals[1]);
            }
            if (vals[0].equals("ip")) {
                ip = (vals[1]);
            }
            if (vals[0].equals("page")) {
                page = (vals[1]);
            }
            if (vals[0].equals("sdate")) {
                sdate = (vals[1]);
            }
            if (vals[0].equals("edate")) {
                edate = (vals[1]);
            }
        }
        List<String> matches = ipLogger.getLogs(ip, page, username, sdate, edate);
        String result = "";
        int count = 0;

        if(matches == null){
            result = "10%No Result";
        }else{
            boolean first = true;
            for(String row : matches){
                count++;
                if(first){
                    result = "10%" + row;
                    first = false;
                }else {
                    result+= "," + row;
                }
            }
        }
        debuggingService.stompDebug("@Stomp Traffic search called by " + search.getUsername() + " completed with " + count + " results");
        matches = null;
        this.simpMessagingTemplate.convertAndSendToUser(search.getStompID(), "/queue/notify", result, createHeaders(search.getStompID()));
    }

    void searchTransactions(StompUserSearch search) throws Exception{
        String result = "", sdate = "", edate = "";
        int count = 0;
        Map<String, String> request = new HashMap<>();
        for (String par : search.getRequest().split(",")) {
            String[] vals = par.split(":");
            if (vals[0].equals("username")) {
                request.put("username", vals[1]);
            }
            if (vals[0].equals("operation")) {
                request.put("operation", vals[1]);
            }
            if (vals[0].equals("successful")) {
                if(vals[1].contains("t")){
                    request.put("success", "true");
                }else{
                    request.put("success", "false");
                }
            }
            if (vals[0].equals("sdate")) {
                sdate = (vals[1]);
            }
            if (vals[0].equals("edate")) {
                edate = (vals[1]);
            }
        }
        List<Transaction> matches = transactionService.getTransactions(request);
        List<Transaction> fmatches = new ArrayList<>();
        if(!sdate.equals("")){
            String[] temp = sdate.split("~");
            String[] temp1 = temp[0].split("-");
            long stime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                    , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
            if(!edate.equals("")){
                temp = edate.split("~");
                temp1 = temp[0].split("-");
                long etime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                        , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
                for(Transaction transaction : matches){
                    if(transaction.getFinishTime() >= stime && transaction.getFinishTime() <= etime){
                        fmatches.add(transaction);
                    }
                }
                matches = fmatches;
            }else{
                for(Transaction transaction : matches){
                    if(transaction.getFinishTime() >= stime){
                        fmatches.add(transaction);
                    }
                }
                matches = fmatches;
            }
        }else if(!edate.equals("")){
            String[] temp = edate.split("~");
            String[] temp1 = temp[0].split("-");
            long etime = CalenderConverter.getMillis(Integer.parseInt(temp1[0]), Integer.parseInt(temp1[1]), Integer.parseInt(temp1[2])
                    , Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
            for(Transaction transaction : matches){
                if(transaction.getFinishTime() <= etime){
                    fmatches.add(transaction);
                }
            }
            matches = fmatches;
        }

        boolean first = true;
        if(matches.isEmpty()){
            result = "10%No Result";
        }else{
            for(Transaction transaction : matches){
                if(first){

                    result = "10%username:" + transaction.getUsername() + ";operation:" + transaction.getOperation() + ";successful:" + transaction.isSuccessfull()
                            + ";Transaction ID:" + transaction.getTransactionID().toString() + ";Time:"
                            + CalenderConverter.getMonthDayYearHourMinuteSecond(transaction.getFinishTime(), "/", "~");
                    first = false;
                }else {
                    result+= ",username:" + transaction.getUsername() + ";operation:" + transaction.getOperation() + ";successful:" + transaction.isSuccessfull()
                            + ";Transaction ID:" + transaction.getTransactionID().toString() + ";Time:"
                            + CalenderConverter.getMonthDayYearHourMinuteSecond(transaction.getFinishTime(), "/", "~");
                }
                //transaction = null;
            }
        }
        debuggingService.stompDebug("@Stomp Transaction search called by " + search.getUsername() + " completed with " + matches.size() + " results");
        matches = null;
        this.simpMessagingTemplate.convertAndSendToUser(search.getStompID(), "/queue/notify", result, createHeaders(search.getStompID()));
    }

    private synchronized void pingUpdate(List<String[]> newList) {
        pings = new ArrayList<>(newList);
    }

    //@Scheduled(fixedRate=2000)
    void sendSysStats(StompRegisterRequest request) {
        this.simpMessagingTemplate.convertAndSendToUser(request.getSession(), "/queue/notify", "stat%" + systemStatsService.getSystemStats(), createHeaders(request.getSession()));
        //this.simpMessagingTemplate.convertAndSend("/maint/table", systemStatsService.getTableStats());
        //stompDebug("@Stomp Send system stats");
    }

    String getTableStats() {
        return systemStatsService.getTableStats();
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

}