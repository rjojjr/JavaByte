package com.kirchnersolutions.database.core.tables;

import com.kirchnersolutions.database.Servers.HTTP.beans.UserListBean;
import com.kirchnersolutions.database.objects.User;
import com.kirchnersolutions.database.sessions.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@DependsOn({"TableManagerService", "UserRepository"})
@Service
@ApplicationScope
@DependsOn("userRepository")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService() throws Exception {

    }

    /**
     * Logs user on.
     * Returns null if credentials don't match;
     *
     * @param userName
     * @param Password
     * @param sessionType
     * @return
     * @throws Exception
     */
    public User logOn(String userName, BigInteger Password, String sessionType) throws Exception {
        return LogOn(userName, Password, sessionType);
    }

    /**
     * Logs user off.
     *
     * @param user
     * @param action
     * @param sessionType
     * @return
     * @throws Exception
     */
    public boolean logOff(User user, int action, String sessionType) throws Exception {
        return LogOff(user, action, sessionType);
    }

    /**
     * Creates new user with given values.
     * Returns false if value format is invalid or username already exists.
     *
     * @param values
     * @return
     * @throws Exception
     */
    public boolean createUser(Map<String, String> values) throws Exception {
        return CreateUser(values);
    }

    /**
     * Gets user from repository.
     * Returns null if user is not in repository.
     * @param username
     * @return
     */
    public User getUserFromRepo(String username){
        return GetUserFromRepo(username);
    }

    /**
     * Allows admin to edit user.
     * Returns null if unsuccessful.
     * @param admin
     * @param subject
     * @return
     * @throws Exception
     */
    public UserListBean editUser(Session admin, UserListBean subject) throws Exception{
        return EditUser(admin, subject);
    }

    /**
     *
     * @param userSession
     * @param current
     * @param New
     * @param confirm
     * @return
     * @throws Exception
     */
    public boolean resetPassword(Session userSession, String current, String New, String confirm) throws Exception{
        return  ResetPassword(userSession, current, New, confirm);
    }

    public List<User> searchUserDB(Map<String, String> request) throws Exception{
        return SearchUserDB(request);
    }

    public boolean adminResetPassword(Session admin, String newPassword, String userID){
        return AdminResetPassword(admin, newPassword, userID);
    }

    private User GetUserFromRepo(String username){
        Map<String, String> values = new HashMap<>();
        values.put(new String("username"), username);
        List<User> user = userRepository.searchRepository(values);
        if(user.size() != 1){
            return null;
        }
        return user.get(0);
    }

    private boolean ResetPassword(Session userSession, String current, String New, String confirm) throws Exception{
        User user = userSession.getUser();
        return userRepository.resetPasword(user, current, New, confirm);
    }

    private UserListBean EditUser(Session admin, UserListBean subject) throws Exception{
        return userRepository.editUser(admin, subject);
    }

    private boolean CreateUser(Map<String, String> values) throws Exception {
        return userRepository.createUser(values);
    }

    private User LogOn(String userName, BigInteger Password, String sessionType) throws Exception {
        return userRepository.logOn(userName, Password, sessionType)
;    }

    private List<User> SearchUserDB(Map<String, String> request) throws Exception{
        //udbSystem.out.println("Before userRepository db search");
        return userRepository.searchDB(request);
    }

    private boolean LogOff(User user, int action, String sessionType) throws Exception {
        return userRepository.logOff(user, action, sessionType);
    }

    private boolean AdminResetPassword(Session admin, String newPassword, String userID){
        return userRepository.adminResetPassword(admin, newPassword, userID);
    }

}