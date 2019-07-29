package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.Servers.HTTP.beans.DeviceCertificate;
import com.kirchnersolutions.database.core.tables.TableManagerService;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.database.sessions.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.util.*;

@DependsOn("tableManagerService")
@Repository
public class DeviceRepository {

    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private SysVars sysVars;
    @Autowired
    private SessionService sessionService;

    private List<DeviceCertificate> activeDevices = Collections.synchronizedList(new ArrayList<>());

    public DeviceRepository() throws Exception {

    }

    @PostConstruct
    public void init() throws Exception{
        File deviceTable = new File(tableManagerService.getTablesDir(), sysVars.getFileSeperator() + "Devices");
        if (!deviceTable.exists()) {
            String[] fields = new String[4];
            fields[0] = "certificate-i";
            fields[1] = "certifiedbyIndex-i";
            fields[2] = "type-s";
            fields[3] = "createdtime-i";
            try {
                tableManagerService.createNewTable(new String("Devices"), fields, null);
            } catch (Exception e) {
                throw e;
            }
        }
        deviceTable = new File(tableManagerService.getTablesDir(), sysVars.getFileSeperator() + "DeviceLogs");
        if (!deviceTable.exists()) {
            String[] fields = new String[5];
            fields[0] = "deviceindex-i";
            fields[1] = "userindex-i";
            fields[2] = "action-s";
            fields[3] = "ip-s";
            fields[4] = "time-i";
            try {
                tableManagerService.createNewTable(new String("DeviceLogs"), fields, null);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * Returns true if device exists.
     *
     * @param cert
     * @return
     * @throws Exception
     */
    boolean deviceCertExists(DeviceCertificate cert) throws Exception {
        return DeviceCertExists(cert);
    }

    /**
     * Creates device if it does not exist.
     *
     * @param cert
     * @return
     * @throws Exception
     */
    boolean createDevice(DeviceCertificate cert) throws Exception {
        return CreateDevice(cert);
    }

    /**
     * Returns device matching request parameters from repository.
     * Returns null if no device exists in repository.
     *
     * @param request
     * @return
     */
    DeviceCertificate getDeviceFromRepo(String[][] request) {
        return GetDeviceFromRepo(request);
    }

    List<DeviceCertificate> getAllActiveDevices(){
        return new ArrayList<>(activeDevices);
    }

    /**
     * Returns list of devices from table that match criteria.
     * Returns null if no result.
     * @param request
     * @return
     */
    List<DeviceCertificate> getDeviceFromTable(String[][] request, int howMany) throws Exception{
        return GetDeviceFromTable(request, howMany);
    }

    boolean addDeviceToRepo(DeviceCertificate device, String action) throws Exception{
        return AddDeviceToRepo(device, action);
    }

    boolean removeDeviceFromRepo(DeviceCertificate device, String action) throws Exception{
        return RemoveDeviceFromRepo(device, action);
    }

    private boolean DeviceCertExists(DeviceCertificate cert) throws Exception {
        BigInteger hash = cert.getCert();
        Map<String, String> request = new HashMap<>();
        request.put(new String("certificate"), new String(hash.toString()));
        List<Map<String, String>> result = tableManagerService.searchTable(new String("Devices"), request, 1);
        if (result == null || result.isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean CreateDevice(DeviceCertificate cert) throws Exception {
        if (DeviceCertExists(cert)) {
            return false;
        }
        List<Map<String, String>> request = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        Session session = sessionService.getSessionByUsername(cert.getUsername());
        if (session == null) {
            //System.out.println("Requesting device cert");
            //System.out.println(cert.getUsername());
            return false;
        }
        System.out.println(session.getUserIndex());
        row.put(new String("certifiedbyIndex"), session.getUserIndex());
        row.put(new String("certificate"), new String(cert.getCert().toString()));
        row.put(new String("type"), cert.getType());
        row.put(new String("createdtime"), new String(System.currentTimeMillis() + ""));
        request.add(row);
        List<BigInteger> indexs = tableManagerService.createRowsReturnIndexs(request, new String("Devices"));
        if (indexs == null || indexs.isEmpty()) {
            return false;
        }
        request = new ArrayList<>();
        row = new HashMap<>();
        row.put(new String("userindex"), session.getUserIndex());
        row.put(new String("deviceindex"), new String(indexs.get(0).toString()));
        row.put(new String("action"), new String("create"));
        row.put(new String("ip"), new String(cert.getIp()));
        row.put(new String("time"), new String(System.currentTimeMillis() + ""));
        request.add(row);
        tableManagerService.createRows(request, new String("DeviceLogs"));
        activeDevices.add(cert);
        return true;
    }

    private DeviceCertificate GetDeviceFromRepo(String[][] request) {
        for (DeviceCertificate cert : activeDevices) {
            boolean found = true;
            for (String[] par : request) {
                if (par[0].equals(new String("certificate"))) {
                    if (!par[1].equals(new String(cert.getCert().toString()))) {
                        found = false;
                        break;
                    }
                }
                if (par[0].equals(new String("index"))) {
                    if (!par[1].equals(new String(cert.getIndex().toString()))) {
                        found = false;
                        break;
                    }
                }
                if (par[0].equals(new String("username"))) {
                    if (!par[1].equals(cert.getUsername())) {
                        found = false;
                        break;
                    }
                }
                if (par[0].equals(new String("ip"))) {
                    if (!par[1].equals(cert.getIp())) {
                        found = false;
                        break;
                    }
                }
                if (par[0].equals(new String("type"))) {
                    if (!par[1].equals(cert.getType())) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                return cert;
            }
        }
        return null;
    }

    private List<DeviceCertificate> GetDeviceFromTable(String[][] request, int howMany) throws Exception{
        List<DeviceCertificate> results = new ArrayList<>();
        Map<String, String> req = new HashMap<>();
        for (String[] par : request) {
            if (par[0].equals(new String("certificate"))) {
                req.put(new String("certificate"), par[1]);
            }
            if (par[0].equals(new String("index"))) {
                req.put(new String("index"), par[1]);
            }
            if (par[0].equals(new String("username"))) {
                req.put(new String("certifiedbyindex"), par[1]);
            }
            if (par[0].equals(new String("type"))) {
                req.put(new String("type"), par[1]);
            }
            if (par[0].equals(new String("time"))) {
                req.put(new String("createdtime"), par[1]);
            }
        }
        List<Map<String, String>> result = tableManagerService.searchTable(new String("Devices"), req, howMany);
        if(result == null || result.isEmpty()){
            return null;
        }
        for(Map<String, String> dev : result){
            String[][] active = new String[1][2];
            active[0][0] = new String("certificate");
            active[0][1] = dev.get(new String("certificate"));
            DeviceCertificate cert = GetDeviceFromRepo(active);
            if(cert != null){
                results.add(cert);
            }else{
                cert = new DeviceCertificate(new BigInteger(dev.get(new String("certificate")).toString()));
                cert.setType(dev.get(new String("type")));
                cert.setIndex(new BigInteger(dev.get(new String("index")).toString()));
                results.add(cert);
            }
        }
        return results;
    }

    private boolean AddDeviceToRepo(DeviceCertificate device, String action) throws Exception{
        if(LogDeviceAction(device, action)){
            activeDevices.add(device);
            return true;
        }
        return false;
    }

    private boolean RemoveDeviceFromRepo(DeviceCertificate device, String action) throws Exception{
        String[][] active = new String[1][2];
        active[0][0] = new String("certificate");
        active[0][1] = new String(device.getCert().toString());
        DeviceCertificate cert = GetDeviceFromRepo(active);
        if(cert != null){
            if(LogDeviceAction(device, action)){
                activeDevices.remove(cert);
                return true;
            }
            return false;
        }
        return false;
    }

    boolean LogDeviceAction(DeviceCertificate device, String action) throws Exception{
        List<Map<String, String>> request = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        Session session = sessionService.getSessionByUsername(device.getUsername());
        if (session == null) {
            return false;
        }
        row.put(new String("userindex"), session.getUserIndex());
        row.put(new String("deviceindex"), new String(device.getIndex().toString()));
        row.put(new String("action"), action);
        row.put(new String("ip"), new String(device.getIp()));
        row.put(new String("time"), new String(System.currentTimeMillis() + ""));
        request.add(row);
        tableManagerService.createRows(request, new String("DeviceLogs"));
        return true;
    }

}