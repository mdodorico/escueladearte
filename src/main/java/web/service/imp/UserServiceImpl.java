package web.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.bytebuddy.utility.RandomString;
import web.exception.UserNotFoundException;
import web.model.Role;
import web.model.Users;
import web.repository.RoleRepository;
import web.repository.UserRepository;
import web.service.UserService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private RoleRepository roleRepository;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
 
	@Override
    public Users findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
	
	@Override
	public List<Users> findAllUsers() {
		return userRepository.findAll();
	}
	
	@Override
	public List<Role> findAllRoles() {
		return roleRepository.findAll();
	}

	@Override
	public Users findById(Integer id) {
		Optional<Users> optional = userRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
	
	@Override
    public void save(Users user, String siteURL) throws UnsupportedEncodingException, MessagingException {
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		if(user.getRole() == null) {
			Role role = new Role();
			role.setId(3);
			user.setRole(role);
		} else {
			Role role = (Role) user.getRole();
			role.setId(role.getId());
			user.setRole(role);
		}
		
		String randomCode = RandomString.make(64);
		user.setVerificationCode(randomCode);
		user.setEnabled(false);
		
		userRepository.save(user);
		
		sendVerificationEmail(user, siteURL);
    }
	
	private void sendVerificationEmail(Users user, String siteURL) throws MessagingException, UnsupportedEncodingException {
		String toAddress = user.getEmail();
		String fromAddress = "escuelarte.vangogh@gmail.com";
		String senderName = "Escuela Van Gogh";
		String subject = "Por favor verifique su registro";
		String content = "Estimado/a [[name]],<br>"
				+ "Por favor haga click en el siguiente link para verificar su cuenta:<br><br>"
				+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFICAR</a></h3><br><br>"
				+ "Muchas gracias,<br>"
				+ "Marian D'Odorico";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		content = content.replace("[[name]]", user.getFullName());
		String verifyURL = siteURL + "/verify?code=" + user.getVerificationCode();
		
		content = content.replace("[[URL]]", verifyURL);
		
		helper.setText(content, true);
		
		mailSender.send(message);
		
		System.out.println("Email has been sent");
	}

	@Override
	public boolean verify(String verificationCode) {
        Users user = userRepository.findByVerificationCode(verificationCode);
		
		if (user == null || user.getEnabled()) {
			return false;
		} else {
			user.setVerificationCode(null);
			user.setEnabled(true);
			userRepository.save(user);
			
			return true;
		}
	}
	
	@Override
	public void delete(Integer[] id) {
		for(Integer idUser: id) {
			deleteImage(idUser);
			userRepository.deleteById(idUser);
		}
	}
	
	@Override
	public void deleteProfile(Integer id) {
		deleteImage(id);
		userRepository.deleteById(id);
	}
	
	@Override
	public void deleteUser(Integer id) {
		deleteImage(id);
		userRepository.deleteById(id);
	}

	@Override
	public void update(Users user) {
		deleteImage(user.getId());
		userRepository.save(user);
	}
	
	@Transactional
    private void deleteImage(Integer idUser) {
		Users user = findById(idUser);
		String avatar = "avatar.png";
		
		if(user.getPhoto().equals(avatar)) {
			System.out.println("La imagen no se eliminará porque es avatar.png");
		} else {
			try { 
		    	 Files.deleteIfExists(Paths.get("c:/arte/images/users/" + user.getPhoto()));
		        }
		     catch (NoSuchFileException e) {
		            System.out.println("No existe el directorio o archivo");
		        }
		     catch (DirectoryNotEmptyException e) {
		            System.out.println("El directorio no está vacío");
		        }
		     catch (IOException e) {
		            System.out.println("No tiene permisos para realizar esta operación");
		        }
		 
		     System.out.println("La imagen fue eliminada");
		    }
	 }
	
	@Override
	public void resendEmail(Users user, String siteURL) throws MessagingException, UnsupportedEncodingException {
		String randomCode = RandomString.make(64);
		user.setVerificationCode(randomCode);
		userRepository.save(user);
		sendVerificationEmail(user, siteURL);
	}
	
	@Override
	public void updateResetPasswordToken(String token, String email) throws UserNotFoundException {
        Users customer = userRepository.findByEmail(email);
        if (customer != null) {
            customer.setResetPasswordToken(token);
            userRepository.save(customer);
        } else {
            throw new UserNotFoundException("No se encontró a un usuario con el email " + email);
        }
    }
    
	@Override
    public Users getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }
    
	@Override
    public void updatePassword(Users user, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
         
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }
}
