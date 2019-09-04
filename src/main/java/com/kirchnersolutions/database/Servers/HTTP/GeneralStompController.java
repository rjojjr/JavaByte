package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.DeviceConfiguration;
import com.kirchnersolutions.database.Servers.HTTP.beans.*;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.sessions.SessionService;
import com.kirchnersolutions.database.sessions.WebSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigInteger;

@DependsOn({"debuggingService", "socketStompService"})
@Controller
public class GeneralStompController {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private DebuggingService debuggingService;
    @Autowired
    private HTTPService httpService;
    @Autowired
    private GeneralStompService generalStompService;
    @Autowired
    private DeviceConfiguration deviceConfiguration;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private TableStompService tableStompService;
    @Autowired
    private LogStompService logStompService;
    @Autowired
    private SocketStompService socketStompService;

    /*
    @Scheduled(fixedDelay=2000)
    private void sendMsgs(){
        List<Ping> msgs = userEntity.getStompMessages();
        System.out.println(msgs.size() + "que size");
        String queue = "/queue/specific-user";
        for(Ping ping : msgs){
            simpMessagingTemplate.convertAndSendToUser(ping.getIndex(), queue, ping);
        }
    }
    */

    //@MessageMapping("/")
    @MessageMapping("/register")
    public void registerUsername(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        if(!username.getUsername().equals("null")){
            if(sessionService.setStompID(new String(username.getUsername()), username.getSession())){
                debuggingService.stompDebug("@Stomp Registered username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            }else{
                debuggingService.stompDebug("@Stomp Registered username: " + username.getUsername() + " Stomp ID: " +
                        username.getSession() + " failed, session not authentic.");
                denyRequest("Your session is not authentic", username.getSession());
            }
        }
    }

    @MessageMapping("/httpstatus")
    public void checkHttpStatus(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        sessionService.setStompID(new String(username.getUsername()), username.getSession());
        debuggingService.stompDebug("@Stomp Registered username: " + username.getUsername() + " Stomp ID: " + username.getSession());
        debuggingService.stompDebug("@Stomp Checking user " + username.getUsername() + " http status");
        String status = httpService.checkHttpStatus(new String(username.getUsername()));
        if (status.equals("logged off")) {
            debuggingService.stompDebug("@Stomp User " + username.getUsername() + " corrupt session");
        }
        if (status.equals("invalid")) {
            debuggingService.stompDebug("@Stomp User " + username.getUsername() + " http session is invalid... Refreshing...");
            SystemStompMessage msg = new SystemStompMessage();
            msg.setIndex(username.getSession());
            msg.setPage("2%null");
        }
        if (status.equals("valid")) {
            debuggingService.stompDebug("@Stomp User " + username.getUsername() + " http session is valid.");

        }
    }

    //Devices
    @MessageMapping("/device/cert")
    public void registerDevice(@Payload StompDeviceCert device, SimpMessageHeaderAccessor headerAccessor) {
        String cert = device.getCert();
        System.out.println(cert);
        //System.out.println(device.getUser());
        if (cert.equals("0")) {
            if (deviceConfiguration.isOnlyRegDevices()) {
                System.out.println("@Stomp Device certificate requested by session " + device.getUser() + " at address " + device.getIp());
                if (deviceConfiguration.isManualDevReg()) {
                    debuggingService.stompDebug("@Stomp Device certificate denied. Manual certificate installation is enabled.");
                } else {
                    debuggingService.stompDebug("@Stomp Device certificate request must be approved by admin.");
                }
            } else {
                try {
                    DeviceCertificate deviceCertificate = deviceService.registerNewDevice(new String("pc"), device.getIp(), device.getUser());
                    if (deviceCertificate == null) {
                        debuggingService.stompDebug("@Stomp Device certificate requested by session " + device.getUser() + " at address " + device.getIp());
                        debuggingService.stompDebug("@Stomp Operation for address " + " at address " + device.getIp() + "failed with unknown cause");
                    } else {
                        debuggingService.stompDebug("@Stomp Device certificate requested by session " + device.getUser() + " at address " + device.getIp());
                        debuggingService.stompDebug("@Stomp Certificate " + deviceCertificate.getCert().toString() + " issued to session " + device.getUser() + " at address " + device.getIp());
                    }

                } catch (Exception e) {
                    debuggingService.stompDebug("@Stomp Device certificate requested by session " + device.getUser() + " at address " + device.getIp());
                    debuggingService.stompDebug("@Stomp Operation failed: " + e.getMessage());
                }
            }
        } else if (cert.equals("-1")) {
            debuggingService.stompDebug("@Stomp Device certificate requested by session at address " + device.getIp() + " unable to write cert.");
        } else {
            try {
                deviceService.logonDevice(new BigInteger(cert), device.getIp(), device.getUser());
                debuggingService.stompDebug("@Stomp Device certificate at address " + device.getIp() + " Cert: " + device.getCert());
            } catch (Exception e) {
                debuggingService.stompDebug("@Stomp Failed to log device cert " + cert + " exception: " + e.getMessage());
            }

        }
    }

    //Table Console
    @MessageMapping("/table/query")
    public void getTableQuery(StompTableRequest request) throws Exception{
        tableStompService.queryTable(request);
    }

    @MessageMapping("/table/query/record")
    public void getRow(StompTableRequest request) throws Exception{
        tableStompService.queryRecord(request);
    }

    @MessageMapping("/table/commit/record")
    public void commit(StompTableRequest request) throws Exception{
        tableStompService.commit(request);
    }

    @MessageMapping("/table/create")
    public void createTable(StompTableRequest request) throws Exception{
        tableStompService.createTable(request);
    }

    @MessageMapping("/table/info")
    public void getTableInfo(StompTableRequest request){
        tableStompService.getTableInfo(request);
    }

    //Maint. Console
    @MessageMapping("/maint/tab")
    @SendTo("/maint/table")
    public String getStats(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session.getUser().getDetail("admin").contains("t")){
            debuggingService.stompDebug("@Stomp Table Stats requested by " + username.getUsername());
            return generalStompService.getTableStats();
        }else{
            debuggingService.stompDebug("@Stomp Table Stats requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
            return "denied";
        }

    }

    @MessageMapping("/maint/pinginit")
    @SendTo("/maint/ping/init")
    public String pingInit(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            generalStompService.pingInit(username.getSession());
        }else{
           //debuggingService.stompDebug("@Stomp Ping requested by " + username.getUsername() + " denied due to lack of privilege");
            //denyRequest("You do not have privilege for this function.", username.getSession());
        }
        return "";
    }

    @MessageMapping("/maint/pingfin")
    @SendTo("/maint/ping/fin")
    public String pingFin(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        return "" + generalStompService.pingResult(username.getSession());
    }

    @MessageMapping("/maint/bench")
    public void bench(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            generalStompService.benchCPU(username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp System bench requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/reboot")
    public void reboot(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            generalStompService.reboot(username.getSession(), session);
        }else{
            debuggingService.stompDebug("@Stomp Application reboot requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/shutdown")
    public void shutdown(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            generalStompService.shutdown(username.getSession(), session);
        }else{
            debuggingService.stompDebug("@Stomp Application shutdown requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/get/sys")
    public void getSysStats(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            generalStompService.sendSysStats(username);
        }else{
            //debuggingService.stompDebug("@Stomp System stats requested by " + username.getUsername() + " denied due to lack of privilege");
            //denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/bench/stop")
    public void stopBench(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        debuggingService.stompDebug("@Stomp Benchmark stop called by " + username.getUsername());
        generalStompService.stopCPUBench();
    }

    @MessageMapping("/maint/changelog")
    public void getChangelog(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(username.getUsername()), username.getSession());
            debuggingService.stompDebug("@Stomp Changelog requested by username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            generalStompService.getChangelog(username.getUsername(), username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp Changelog requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/doc")
    public void getDocs(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(username.getUsername()), username.getSession());
            debuggingService.stompDebug("@Stomp Documentation requested by username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            generalStompService.getDoc(username.getUsername(), username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp Document requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/socket/stat")
    public void getSocketStat(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(username.getUsername()), username.getSession());
            debuggingService.stompDebug("@Stomp Socket server status requested by username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            socketStompService.getSocketStat(username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp Socket server status requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/socket/start")
    public void startSocket(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(username.getUsername()), username.getSession());
            debuggingService.stompDebug("@Stomp Socket server start requested by username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            socketStompService.startSocket(username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp Socket server start requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/socket/shutdown")
    public void stopSocket(@Payload StompRegisterRequest username, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(username.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(username.getUsername()), username.getSession());
            debuggingService.stompDebug("@Stomp Socket server shutdown requested by username: " + username.getUsername() + " Stomp ID: " + username.getSession());
            socketStompService.shutdownSocket(username.getSession());
        }else{
            debuggingService.stompDebug("@Stomp Socket server shutdown requested by " + username.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", username.getSession());
        }
    }

    @MessageMapping("/maint/var/get")
    public void getVars(@Payload StompTableRequest request, SimpMessageHeaderAccessor headerAccessor) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSessionid());
            debuggingService.stompDebug("@Stomp Variables requested by username: " + request.getUsername() + " Stomp ID: " + request.getSessionid());
            generalStompService.getVars(request);
        }else{
            debuggingService.stompDebug("@Stomp Variable requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getSessionid());
        }
    }

    @MessageMapping("/maint/var/set")
    public void setVars(@Payload StompTableRequest request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSessionid());
            debuggingService.stompDebug("@Stomp Set variables requested by username: " + request.getUsername() + " Stomp ID: " + request.getSessionid());
            generalStompService.setVars(request);
        }else{
            debuggingService.stompDebug("@Stomp Set variables requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getSessionid());
        }
    }

    @MessageMapping("/users/search")
    public void userSearch(@Payload StompUserSearch request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getStompID());
            debuggingService.stompDebug("@Stomp User search called by " + request.getUsername());
            generalStompService.searchUsers(request);
        }else{
            debuggingService.stompDebug("@Stomp User search requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getStompID());
        }
    }

    @MessageMapping("/usermessage")
    public void senduserMsgs(@Payload SystemStompMessage msg, SimpMessageHeaderAccessor headerAccessor) {
        debuggingService.stompDebug("@Stomp Send Message Request by: " + msg.getIndex());
        sendUserMessage(msg.getPage(), msg.getIndex());
    }

    //Traffic

    @MessageMapping("/traffic/search")
    public void trafficSearch(@Payload StompUserSearch request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getStompID());
            debuggingService.stompDebug("@Stomp Traffic search called by " + request.getUsername());
            generalStompService.searchTraffic(request);
        }else{
            debuggingService.stompDebug("@Stomp Traffic search requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getStompID());
        }
    }

    @MessageMapping("/traffic/count")
    public void trafficCount(@Payload StompRegisterRequest request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSession());
            logStompService.getIPLogNumber(request);
        }else{
            denyRequest("You do not have privilege for this function.", request.getSession());
        }
    }

    @MessageMapping("/traffic/dump")
    public void trafficDump(@Payload StompRegisterRequest request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSession());
            logStompService.dumpIPLogs(request);
        }else{
            denyRequest("You do not have privilege for this function.", request.getSession());
        }
    }

    //Loggs

    @MessageMapping("/logs/search")
    public void logsSearch(@Payload StompTableRequest request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSessionid());
            debuggingService.stompDebug("@Stomp Log search called by " + request.getUsername());
            logStompService.searchLogs(request);
        }else{
            debuggingService.stompDebug("@Stomp Log search requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getSessionid());
        }
    }

    @MessageMapping("/logs/dump")
    public void dumpLogs(@Payload StompTableRequest request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getSessionid());
            debuggingService.stompDebug("@Stomp " + request.getTablename() + " Log dump called by " + request.getUsername());
            logStompService.dumpLogs(request.getTablename(), request.getSessionid());
        }else{
            debuggingService.stompDebug("@Stomp " + request.getTablename() + " Log dump requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getSessionid());
        }
    }

    //Transactions

    @MessageMapping("/transaction/search")
    public void transactionSearch(@Payload StompUserSearch request, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        WebSession session = (WebSession)sessionService.getSessionByUsername(request.getUsername());
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            sessionService.setStompID(new String(request.getUsername()), request.getStompID());
            debuggingService.stompDebug("@Stomp Transaction search called by " + request.getUsername());
            generalStompService.searchTransactions(request);
        }else{
            debuggingService.stompDebug("@Stomp Transaction search requested by " + request.getUsername() + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", request.getStompID());
        }
    }

    private void sendUserMessage(String msg, String from) {
        WebSession session = (WebSession)sessionService.getSessionByUsername(from);
        if(session != null && session.getUser().getDetail("admin").contains("t")){
            UserMessageBean mesg = new UserMessageBean(from);
            String[] temp1 = msg.split("%");
            String message = temp1[1];
            mesg.setMessage(message);
            String[] users = msg.split(",");
            if(users.length < 2){
                String[] temp = msg.split("%");
                mesg.setUser(temp[0]);
            }else{
                for (String user : users) {
                    if (user.split("%").length == 2 && !user.split("%")[1].equals("")) {
                        String[] temp = user.split("%");
                        mesg.setUser(temp[0]);
                        //mesg.setMessage(message);
                    } else {
                        mesg.setUser(user);
                        //mesg.setMessage(message);
                    }
                }
            }
            //System.out.println("Send msg");
            generalStompService.sendUserMsgs(mesg);
        }else {
            debuggingService.stompDebug("@Stomp Send message requested by " + from + " denied due to lack of privilege");
            denyRequest("You do not have privilege for this function.", session.getStompID());
        }
    }

    @MessageMapping("/backup/table")
    public void backupTable(StompTableRequest request) throws Exception{
        try{
            socketStompService.backUpTable(request.getSessionid(), request.getTablename());
        }catch (Exception e){
            e.printStackTrace();
            debuggingService.throwDevException(new DevelopmentException("Failed to backup table " + e.getMessage()));
            debuggingService.nonFatalDebug("Failed to backup table " + e.getMessage());
        }
    }

    @MessageMapping("/backup/all")
    public void backupTableAll(StompTableRequest request) throws Exception{
        try{
            socketStompService.backUpTables(request.getSessionid());
        }catch (Exception e){
            e.printStackTrace();
            debuggingService.throwDevException(new DevelopmentException("Failed to backup tables " + e.getMessage()));
            debuggingService.nonFatalDebug("Failed to backup tables " + e.getMessage());
        }
    }

    @MessageExceptionHandler
    public void handleException(Throwable exception) {
        System.out.println(exception.getMessage());
    }

    private void denyRequest(String msg, String sessionID){
        SystemStompMessage msgout = new SystemStompMessage();
        msgout.setIndex(sessionID);
        msgout.setPage("deny%System%" + msg);
        generalStompService.sendMsgs(msgout);
    }

}