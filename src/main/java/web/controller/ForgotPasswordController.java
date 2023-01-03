package web.controller;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import net.bytebuddy.utility.RandomString;
import web.exception.UserNotFoundException;
import web.model.Users;
import web.service.UserService;


@Controller
public class ForgotPasswordController {
	
	@Autowired
    private JavaMailSender mailSender;
     
	@Autowired
	private UserService userService;
     
    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
    	return "password/forgot_password_form";
    }
 
    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
    	String email = request.getParameter("email");
        String token = RandomString.make(30);
         
        try {
        	userService.updateResetPasswordToken(token, email);
            String resetPasswordLink = getSiteURL(request) + "/reset_password?token=" + token;
            sendEmail(email, resetPasswordLink);
            model.addAttribute("message", "Enviamos un link a su correo para recuperar la clave, por favor revíselo.");
             
        } catch (UserNotFoundException ex) {
            model.addAttribute("error", ex.getMessage());
        } catch (UnsupportedEncodingException | MessagingException e) {
            model.addAttribute("error", "Error al enviar el email.");
        }
             
        return "password/forgot_password_form";
    }
    
    private String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}
    
    public void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();              
        MimeMessageHelper helper = new MimeMessageHelper(message);
         
        helper.setFrom("escuelarte.vangogh@gmail.com", "Soporte Escuela Van Gogh");
        helper.setTo(recipientEmail);
        String subject = "Link para recuperar su clave";
        String content = "<p>Hola!</p>"
                + "<p>Recibimos una solicitud para resetear la clave de su cuenta.</p>"
                + "<p>Haga click en el siguiente link para generar una nueva contraseña:</p>"
                + "<p><a href=\"" + link + "\">Cambiar clave</a></p>"
                + "<br>"
                + "<p>Por favor desestime este email si recuerda su clave, "
                + "o si no realizó el pedido.</p>"
                + "<p>Muchas gracias!</p>";
         
        helper.setSubject(subject); 
        helper.setText(content, true);
        mailSender.send(message);
    }
     
     
    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
    	Users user = userService.getByResetPasswordToken(token);
        model.addAttribute("token", token);
         
        if (user == null) {
            model.addAttribute("message", "Token inválido");
            return "message";
        }
         
        return "password/reset_password_form";
    }
     
    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
    	String token = request.getParameter("token");
        String password = request.getParameter("password");
         
        Users user = userService.getByResetPasswordToken(token);
        model.addAttribute("title", "Reset your password");
         
        if (user == null) {
            model.addAttribute("message", "Token inválido");
            return "message";
        } else {           
        	userService.updatePassword(user, password);
             
            model.addAttribute("message", "La clave fue cambiada con éxito");
        }
         
        return "password/success";
    }
}
