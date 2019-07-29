package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.Configuration.DevVars;
import com.kirchnersolutions.database.Configuration.SysVars;
import com.kirchnersolutions.database.Servers.HTTP.beans.UserListBean;
import com.kirchnersolutions.database.dev.DebuggingService;
import com.kirchnersolutions.database.exceptions.DevelopmentException;
import com.kirchnersolutions.database.exceptions.SessionException;
import com.kirchnersolutions.database.exceptions.UserServiceException;
import com.kirchnersolutions.database.objects.Transaction;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.database.sessions.Session;
import com.kirchnersolutions.utilities.CryptTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//@DependsOn({"TableManagerService", "DevVars", "SysVars"})
@Repository
@DependsOn("tableManagerService")
public class UserRepository {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private DevVars devVars;
    @Autowired
    private TableManagerService tableManagerService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private DebuggingService debuggingService;

    private volatile List<User> users = Collections.synchronizedList(new ArrayList<>());
    private volatile AtomicInteger indexCount = new AtomicInteger(0);

    public UserRepository() throws Exception {

    }

    @PostConstruct
    public void init() throws Exception{
        File dir = new File(tableManagerService.getTablesDir(),  "/Users");
        File logDir = new File(tableManagerService.getTablesDir(), "/UserLogs");
        if (!dir.exists()) {
            String[] fields = new String[7];
            fields[0] = "username-s";
            fields[1] = "firstname-s";
            fields[2] = "lastname-s";
            fields[3] = "id-i";
            fields[4] = "password-i";
            fields[5] = "createdtime-i";
            fields[6] = "admin-b";
            try {
                tableManagerService.createNewTable(new String("Users"), fields, null);
            } catch (Exception e) {
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to create Users table " + e.getMessage() + " Class UserService Method Constructor");
                    throw new DevelopmentException("Failed to create Users table " + e.getMessage() + " Class UserService Method Constructor");
                } else {
                    System.out.println("Failed to create Users table " + e.getMessage());
                    throw new UserServiceException("Failed to create Users table " + e.getMessage());
                }
            }
            try {
                List<Map<String, String>> root = new ArrayList<>();
                Map<String, String> values = new HashMap<>();
                values.put("username", "root");
                values.put("firstname", "System");
                values.put("lastname", "Root User");
                values.put("id", "1");
                values.put("password", new BigInteger(CryptTools.getSHA256("Password55#")).toString());
                values.put("createdtime", "" + System.currentTimeMillis());
                values.put("admin", "true");
                root.add(values);
                tableManagerService.createRows(root, new String("Users"));
            } catch (Exception e) {
                throw e;
            }
        }
        if (!logDir.exists()) {
            String[] fields = new String[4];
            fields[0] = "userindex-i";
            fields[1] = "action-s";
            fields[2] = "time-i";
            fields[3] = "type-s";
            try {
                tableManagerService.createNewTable(new String("UserLogs"), fields, null);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * Returns list of user objects that match request parameters.
     * Returns null if no result.
     *
     * @param request
     * @return
     */
    List<User> searchRepository(Map<String, String> request) {
        return SearchRepository(request);
    }

    /**
     * Removes from repository user objects that match request parameters.
     *
     * @param request
     * @return
     */
    boolean removeUsersFromRepository(Map<String, String> request) {
        return RemoveUsers(request);
    }

    List<User> activeUsers() {
        return getUsers();
    }

    /**
     * Adds user to repository.
     * Returns false if user is already in repository.
     *
     * @param user
     * @return
     */
    boolean addUserToRepository(User user) {
        return AddUser(user);
    }

    /**
     * @param admin
     * @param subject
     * @return
     * @throws Exception
     */
    UserListBean editUser(Session admin, UserListBean subject) throws Exception {
        return EditUser(admin, subject);
    }

    /**
     * Search user table.
     * Returns null if no results.
     *
     * @param request
     * @return
     * @throws Exception
     */
    List<User> searchDB(Map<String, String> request) throws Exception {
        return SearchDB(request);
    }

    boolean adminResetPassword(Session admin, String newPassword, String userID){
        return AdminResetPassword(admin, newPassword, userID);
    }

    boolean createUser(Map<String, String> values) throws Exception {
        return CreateUser(values);
    }

    User logOn(String userName, BigInteger Password, String sessionType) throws Exception {
        return LogOn(userName, Password, sessionType);
    }

    void shutDown(){

    }

    boolean logOff(User user, int action, String sessionType) throws Exception {
        return LogOff(user, action, sessionType);
    }

    boolean resetPasword(User user, String current, String newPassword, String confirm) throws Exception {
        return ResetPasword(user, current, newPassword, confirm);
    }

    private boolean AddUser(User user) {
        Map<String, String> request = new HashMap<>();
        request.put(new String("username"), user.getDetail(new String("username")));
        if (SearchRepository(request).size() > 0) {
            return false;
        }
        user.setDetail(new String("repositoryindex"), new String(indexCount.intValue() + ""));
        users.add(indexCount.intValue(), user);
        indexCount.incrementAndGet();
        return true;
    }

    private boolean RemoveUsers(Map<String, String> request) {
        List<User> results = SearchRepository(request);
        if (results == null) {
            return false;
        }
        for (User user : results) {
            try {
                users.remove(Integer.parseInt(user.getDetail(new String("repositoryindex")).toString()));
            } catch (Exception e) {

            }
        }
        return true;
    }

    private boolean CreateUser(Map<String, String> values) throws Exception {
        if (values.size() != 6) {

            return false;
        }
        boolean valid = true;
        values.put(new String("createdtime"), new String(System.currentTimeMillis() + ""));
        for (String key : values.keySet()) {
            if (!key.equals(new String("username")) && !key.equals(new String("firstname")) && !key.equals(new String("lastname")) && !key.equals(new String("id")) && !key.equals(new String("password")) && !key.equals(new String("createdtime")) && !key.equals(new String("admin"))) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            System.out.println("Validity Problem");
            return false;
        }
        List<Map<String, String>> user = new ArrayList<>();
        user.add(values);
        Map<String, String> username = new HashMap<>();
        username.put(new String("username"), values.get(new String("username")));
        tableManagerService.searchTable(new String("Users"), username, 1);
        if (tableManagerService.searchTable(new String("Users"), username, 1).size() == 1) {
            return false;
        }
        try {
            BigInteger index = tableManagerService.createRowsReturnIndexs(user, new String("Users")).get(0);
            passwordService.updatePassword(new String(index.toString()));
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean ResetPasword(User user, String current, String newPassword, String confirm) throws Exception {
        BigInteger currentPassword = new BigInteger(CryptTools.getSHA256(current));
        BigInteger password = new BigInteger(CryptTools.getSHA256(user.getDetail(new String("password")).toString()));
        if (currentPassword.equals(password)) {
            currentPassword = new BigInteger(CryptTools.getSHA256(newPassword));
            password = new BigInteger(CryptTools.getSHA256(confirm));
            if (currentPassword.equals(password)) {
                Map<BigInteger, Map<String, String>> request = new HashMap<>();
                Map<String, String> edit = new HashMap<>();
                edit.put(new String("password"), new String(currentPassword.toString()));
                request.put(new BigInteger(user.getDetail(new String("index")).toString()), edit);
                if (tableManagerService.editRows(request, new String("Users")) != null) {
                    user.setDetail(new String("password"), new String(currentPassword.toString()));
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private UserListBean EditUser(Session admin, UserListBean subject) throws Exception {
        if (admin.getUser().getDetail(new String("admin")).equals(new String("true"))) {
            Transaction transaction = new Transaction();
            transaction.setUsername(admin.getUser().getDetail(new String("username")));
            transaction.setPassword(new BigInteger(admin.getUser().getDetail(new String("password")).toString()));
            transaction.setUserIndex(new BigInteger(admin.getUser().getDetail(new String("index")).toString()));
            Map<String, String> put = new HashMap<>();
            put.put(new String("username"), new String(subject.getUsername()));
            put.put(new String("firstname"), new String(subject.getFirstname()));
            put.put(new String("lastname"), new String(subject.getLastname()));
            put.put(new String("id"), new String(subject.getId()));
            put.put(new String("admin"), new String(subject.getAdmin()));
            Map<String, String> where = new HashMap<>();
            where.put(new String("index"), new String(subject.getIndex()));
            transaction.setPut(put);
            transaction.setWhere(where);
            transaction.setOperation(new String("PUT ADVANCED Users"));
            try {
                byte[] result = transactionService.submitTransaction(transaction, admin);
                if (result.length == 1) {
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Failed to edit user " + subject.getUsername() + " " + e.getMessage());
                throw e;
            }
            where = new HashMap<>();
            where.put(new String("username"), new String(subject.getUsername()));
            List<User> users = SearchRepository(where);
            if (users == null) {
                return subject;
            }
            User user = users.get(0);
            user.setDetail(new String("username"), new String(subject.getUsername()));
            user.setDetail(new String("firstname"), new String(subject.getFirstname()));
            user.setDetail(new String("lastname"), new String(subject.getLastname()));
            user.setDetail(new String("id"), new String(subject.getId()));
            user.setDetail(new String("admin"), new String(subject.getAdmin()));
            return subject;
        } else {
            return null;
        }
    }

    private List<User> SearchRepository(Map<String, String> request) {
        List<User> result = new ArrayList<>();
        for (User user : getUsers()) {
            boolean match = true;
            for (String key : request.keySet()) {
                if (!user.getDetail(key).equals(request.get(key))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                result.add(user);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return result;
    }

    private List<User> SearchDB(Map<String, String> request) throws Exception {
        List<User> users = new ArrayList<>();
        List<Map<String, String>> dbResult = new ArrayList<>();
        //udbSystem.out.println("Before userdb search");
        dbResult = tableManagerService.searchTableAll(new String("Users"), request);
        //udbSystem.out.println("After userdb search");
        if (dbResult != null) {
            //udbSystem.out.println("UserDBResults != null");
            for (Map<String, String> result : dbResult) {
                User user = new User(result);
                users.add(user);
            }
        } else {
            //udbSystem.out.println("UserDBResults == null");
            users = new ArrayList<>();
        }
        return users;
    }

    private void LogUser(User user, String action) throws Exception {
        List<Map<String, String>> newRows = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put(new String("userindex"), user.getDetail(new String("index")));
        row.put(new String("action"), action);
        row.put(new String("time"), user.getDetail(new String("" + System.currentTimeMillis())));
        newRows.add(row);
        tableManagerService.createRows(newRows, new String("UserLogs"));
    }

    private User LogOn(String userName, BigInteger Password, String sessionType) throws Exception {
        User user;
        Map<String, String> request = new HashMap<>();
        request.put(new String("username"), userName);
        request.put(new String("password"), new String(Password.toString()));
        if (searchRepository(request) != null) {
            if (devVars.isDevExceptions()) {
                System.out.println("Failed to logon user " + userName + " User is already logged on Class UserService Method LogOn");
                throw new DevelopmentException("Failed to logon user " + userName + " User is already logged on Class UserService Method LogOn");
            } else {
                System.out.println("Failed to logon user " + userName + " User is already logged on");
                throw new UserServiceException("Failed to logon user " + userName + " User is already logged on");
            }
        }
        List<Map<String, String>> users;
        try {
            users = tableManagerService.searchTable(new String("Users"), request, 1);
        } catch (Exception e) {
            throw e;
        }
        if (users.size() == 0) {
            return null;
        }
        user = new User(users.get(0));
        users = new ArrayList<>();
        Map<String, String> entry = new HashMap<>();
        entry.put(new String("userindex"), user.getDetail(new String("index")));
        entry.put(new String("time"), new String(System.currentTimeMillis() + ""));
        entry.put(new String("action"), new String("1"));
        entry.put(new String("type"), sessionType);
        users.add(entry);
        try {
            tableManagerService.createRows(users, new String("UserLogs"));
        } catch (Exception e) {
            throw e;
        }
        boolean passwordGood = passwordService.checkPassword(user.getDetail(new String("index")));
        if (!passwordGood) {
            user.setDetail(new String("Password Expired"), new String("Password Expired"));
        }
        return user;
    }

    private boolean LogOff(User user, int action, String sessionType) throws Exception {
        if (user != null) {
            Map<String, String> entry = new HashMap<>();
            entry.put(new String("username"), user.getDetail(new String("username")));
            if (searchRepository(entry) == null) {
                return false;
            }
            removeUsersFromRepository(entry);
            entry = new HashMap<>();
            entry.put(new String("user"), user.getDetail(new String("index")));
            entry.put(new String("time"), new String(System.currentTimeMillis() + ""));
            entry.put(new String("action"), new String("1"));
            entry.put(new String("type"), sessionType);
            List<Map<String, String>> request = new ArrayList<>();
            try {
                tableManagerService.createRows(request, new String("UserLogs"));
                user = null;
                return true;
            } catch (Exception e) {
                System.out.println("Failed to logoff user " + user.getDetail(new String("username")) + " " + e.getMessage());
                throw e;
            }

        } else {
            return true;
        }
    }

    private boolean AdminResetPassword(Session admin, String newPassword, String userID){
        if (admin.getUser().getDetail(new String("admin")).equals(new String("true"))) {
            Transaction transaction = new Transaction();
            transaction.setUsername(admin.getUser().getDetail(new String("username")));
            transaction.setPassword(new BigInteger(admin.getUser().getDetail(new String("password")).toString()));
            transaction.setUserIndex(new BigInteger(admin.getUser().getDetail(new String("index")).toString()));
            BigInteger pw = new BigInteger("-1");
            try {
                pw = new BigInteger(CryptTools.getSHA256(newPassword));
            } catch (Exception e) {
                return false;
                //System.out.println("Failed to edit user " + subject.getUsername() + " " + e.getMessage() + " at Class UserRepository method editUser");
            }
            Map<String, String> put = new HashMap<>();
            put.put(new String("password"), pw.toString());
            Map<String, String> where = new HashMap<>();
            where.put("id", userID);
            transaction.setPut(put);
            transaction.setWhere(where);
            transaction.setOperation(new String("PUT ADVANCED Users"));
            try {
                byte[] result = transactionService.submitTransaction(transaction, admin);
                if (result.length == 1) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                /*
                if (devVars.isDevExceptions()) {
                    System.out.println("Failed to edit user " + subject.getUsername() + " " + e.getMessage() + " at Class UserRepository method editUser");
                    throw new DevelopementException("Failed to edit user " + subject.getUsername() + " " + e.getMessage() + " at Class UserRepository method editUser");
                }
                System.out.println("Failed to edit user " + subject.getUsername() + " " + e.getMessage());
                throw new UserServiceException("Failed to edit user " + subject.getUsername() + " " + e.getMessage());

                 */
                return false;
            }
            return true;
        }
        return false;
    }

    private List<User> getUsers() {
        return new ArrayList<>(users);
    }

    private void setUsers(List<User> users) {
        this.users = Collections.synchronizedList(new ArrayList<>(users));
    }

}