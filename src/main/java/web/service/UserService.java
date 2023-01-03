package web.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.mail.MessagingException;

import web.exception.UserNotFoundException;
import web.model.Role;
import web.model.Users;

public interface UserService {
	
	List<Users> findAllUsers();
    List<Role> findAllRoles();
    Users findByEmail(String email);
    Users findById(Integer id);
    void save(Users user,String siteURL) throws UnsupportedEncodingException, MessagingException;
    void update(Users user);
    void delete(Integer[] id);
    void deleteUser(Integer id);
    void deleteProfile(Integer id);
    boolean verify(String verificationCode);
    void updateResetPasswordToken(String token, String email) throws UserNotFoundException;
    Users getByResetPasswordToken(String token);
    void updatePassword(Users user, String newPassword);
    void resendEmail(Users user, String siteURL) throws MessagingException, UnsupportedEncodingException;
}
