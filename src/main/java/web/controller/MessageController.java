package web.controller;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import web.model.Message;

@Controller
@RequestMapping("message")
public class MessageController {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@PostMapping("/save")
	public String guardar(@Valid @ModelAttribute("message") Message message, RedirectAttributes attributes) 
			throws MessagingException, UnsupportedEncodingException{
		
		String toAddress = "mariana8390@gmail.com";
		String fromAddress = "escuelarte.vangogh@gmail.com";
		String senderName = "Escuela Van Gogh";
		String subject = "Nuevo mensaje recibido";
		String content = "<b>De: </b>" + message.getSender() + " (" + message.getEmail() + ") <br>"
		              + " <b>Asunto: </b>" + message.getSubject() + "<br>"
				      + " <p>" + message.getBody();
		
		MimeMessage mail = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail);
		
		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);
		
		mailSender.send(mail);
		sendAutomaticReply(message);
		
		System.out.println(message);
		
		attributes.addFlashAttribute("msg", "Su mensaje fue enviado. ¡Muchas gracias!");
		return "redirect:/contact";
	}
	
	public void sendAutomaticReply(Message message) throws MessagingException, UnsupportedEncodingException {
		String toAddress = message.getEmail();
		String fromAddress = "escuelarte.vangogh@gmail.com";
		String senderName = "Escuela Van Gogh";
		String subject = " Respuesta automática";
		String content = " Estimado/a " + message.getSender() + ":<br> " 
		               + " Su mensaje fue recibido, a la brevedad recibirá una respuesta.<br> "
				       + " ¡Muchas gracias!<br> "
		               + " Escuela Van Gogh<br><br> "
		               + " --------------------------------------------------------------------<br><br> "
				       + " <i>Esta es una copia de su mensaje:</i> <br><br> "
		               + " <b><i>Asunto: </b>" + message.getSubject() + " </i><br> "
				       + " <p><i> " + message.getBody() + " </i> ";
		
		MimeMessage mail = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail);
		
		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);
		
		mailSender.send(mail);
	}
}
