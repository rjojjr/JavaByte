package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.DeviceConfiguration;
import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.Servers.HTTP.beans.SystemStompMessage;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.Random;

@DependsOn("deviceRepository")
@Service
public class DeviceService {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceConfiguration deviceConfiguration;
    @Autowired
    private GeneralStompService generalStompService;
    @Autowired
    private DebuggingService debuggingService;

    /**
     * Returns newly registered device.
     * @param type
     * @param ip
     * @param username
     * @return
     * @throws Exception
     */
    DeviceCertificate registerNewDevice(String type, String ip, String username) throws Exception {
        return RegisterNewDevice(type, ip, username);
    }

    /**
     * Retrieves active device with given certificate.
     * Returns null if no such active device exist.
     * @param cert
     * @return
     */
    DeviceCertificate getActiveDevice(BigInteger cert){
        return GetActiveDevice(cert);
    }

    /**
     * Returns null if no such device exists.
     * Returns empty device if device is already active.
     * @param cert
     * @param ip
     * @param username
     * @return
     * @throws Exception
     */
    DeviceCertificate logonDevice(BigInteger cert, String ip, String username) throws Exception {
        return LogonDevice(cert, ip, username);
    }

    public boolean logoffDevice(DeviceCertificate device) throws Exception{
        return LogoffDevice(device);
    }

    public void logDevice(DeviceCertificate device, String action) throws Exception{
        LogDevice(device, action);
    }

    List<DeviceCertificate> getAllActiveDevices(){
        return GetAllActiveDevices();
    }

    /**
     * Returns null if user is not logged into a device.
     * @param username
     * @return
     */
    DeviceCertificate getActiveDeviceFromUsername(String username){
        return GetActiveDeviceFromUsername(username);
    }

    private DeviceCertificate RegisterNewDevice(String type, String ip, String username) throws Exception {
        Session session = sessionService.getSessionByUsername(username);
        if (deviceConfiguration.isManualDevReg()) {
            if (session != null && session.getUser().getDetail(new String("admin")).equals(new String("true"))) {
                DeviceCertificate device = newDevice(type, ip, username);
                session.setDevice(device);
                return device;
            }else {
                return null;
            }
        } else {
            DeviceCertificate device = newDevice(type, ip, username);
            if(session != null){
                session.setDevice(device);
            }
            return device;
        }

    }

    private List<DeviceCertificate> GetAllActiveDevices(){
        return deviceRepository.getAllActiveDevices();
    }

    private DeviceCertificate GetActiveDevice(BigInteger cert){
        String[][] active = new String[1][2];
        active[0][0] = new String("certificate");
        active[0][1] = new String(cert.toString());
        return deviceRepository.getDeviceFromRepo(active);
    }

    private DeviceCertificate GetActiveDeviceFromUsername(String username){
        String[][] active = new String[1][2];
        active[0][0] = new String("username");
        active[0][1] = new String(username);
        return deviceRepository.getDeviceFromRepo(active);
    }

    private DeviceCertificate LogonDevice(BigInteger cert, String ip, String username) throws Exception {
        DeviceCertificate device = GetActiveDevice(cert);
        if(device == null){
            String[][] active = new String[1][2];
            active[0][0] = new String("certificate");
            active[0][1] = new String(cert.toString());
            List<DeviceCertificate> tableResult = deviceRepository.getDeviceFromTable(active, 1);
            if(tableResult == null || tableResult.isEmpty()){
                return null;
            }
            device = tableResult.get(0);
            device.setIp(ip);
            device.setUsername(username);
            Session session = sessionService.getSessionByUsername(username);
            session.setDevice(device);
            deviceRepository.addDeviceToRepo(device, new String("logon"));
            return device;
        }
        return device;
    }

    private void LogDevice(DeviceCertificate device, String action) throws Exception{
        try{
            deviceRepository.LogDeviceAction(device, action);
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
            debuggingService.nonFatalDebug(e.getMessage());
        }
    }

    private boolean LogoffDevice(DeviceCertificate device) throws Exception{
        try{
            return deviceRepository.removeDeviceFromRepo(device, new String("logoff"));
        }catch (Exception e){
            debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
            debuggingService.nonFatalDebug(e.getMessage());
        }
        return false;
    }


    private static BigInteger generateNewHash(){
        byte[] bytes = new byte[4096];
        Random random = new Random();
        for(int i = 0; i < 1024; i++){
            bytes[i] = (byte)random.nextInt(125);
        }
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            byte[] byteData = md.digest();
            return new BigInteger(byteData);
        }catch (Exception e){
            return new BigInteger("-1");
        }
    }

    private DeviceCertificate newDevice(String type, String ip, String username) throws Exception{
        boolean valid = true;
        DeviceCertificate device = new DeviceCertificate(new BigInteger("-1"));
        while (valid){
            device = new DeviceCertificate(generateNewHash());

            try{
                valid = deviceRepository.deviceCertExists(device);
            }catch (Exception e){
                throw e;
            }
        }
        device.setType(type);
        device.setIp(ip);
        device.setUsername(username);
        Session session = sessionService.getSessionByUsername(username);
        session.setDevice(device);
        try{
            if(deviceRepository.createDevice(device)){
                SystemStompMessage msg = new SystemStompMessage();
                String stomp = sessionService.getStompID(username);
                msg.setIndex(stomp);
                msg.setPage("1%" + device.getCert().toString());
                generalStompService.sendMsgs(msg);
                return device;
            }else{
                return null;
            }
        }catch (Exception e) {
            debuggingService.throwDevException(new DevelopmentException(e.getMessage()));
            debuggingService.nonFatalDebug(e.getMessage());
        }
        return null;
    }

}