package web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import web.file.FileManager;
import web.model.Message;
import web.model.Users;
import web.service.UserService;

@Controller
public class HomeController {
	
	@Autowired
	private UserService userService;
	
	private Authentication authentication;
	
	@GetMapping("/")
	public String mostrarHome(Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			model.addAttribute("user", user);
			return "index";
		} else {
			return "index";
		}
	}
	
	/* =============== MOSTRAR INDEX UNA VEZ LOGUEADO =============== */
	@GetMapping("/index")
    public String index(Model model){
		authentication = SecurityContextHolder.getContext().getAuthentication();
		Users user = userService.findByEmail(authentication.getName());
		model.addAttribute("user", user);
		return "index";
    }
	
	/* =============== LOGIN =============== */
    @GetMapping("/login")
    public String loginForm(Users user, Model model) {
    	model.addAttribute("user", user);
        return "login";
    }
    
    /* =============== REGISTRAR USUARIO NUEVO (CUENTA NO VERIFICADA) =============== */
    @PostMapping("/login/save")
    public String registration(@Valid @ModelAttribute("user") Users user, HttpServletRequest request, RedirectAttributes attributes, 
    		BindingResult result, @RequestParam("profilePicture") MultipartFile multiPart) throws UnsupportedEncodingException, MessagingException, IOException{
    	
    	if(result.hasErrors()) {
			for(ObjectError error : result.getAllErrors()) {
				System.out.println("Ocurrió un error " + error.getDefaultMessage());
			}
			return "login";
		}
    	
    	Users existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            attributes.addFlashAttribute("msg", "Error: El email ya se encuentra registrado");
            return "redirect:/login";
        }
       
        if(!multiPart.isEmpty()) {
			String ruta = "c:/arte/images/users/";
			String nombreImagen = FileManager.saveFile(multiPart, ruta);
			if(nombreImagen != null) {
				user.setPhoto(nombreImagen);
			}
		} 

        userService.save(user, getSiteURL(request));		
		return "register/register_success";
    }
    
    private String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}	
    
    /* =============== VERIFICAR CUENTA Y HABILITAR AL USUARIO =============== */
    @GetMapping("/verify")
	public String verifyUser(@Param("code") String code) {
		if (userService.verify(code)) {
			return "register/verify_success";
		} else {
			return "register/verify_fail";
		}
	}
    
	/* =============== HORARIOS =============== */
	@GetMapping("/classes")
	public String classes(Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			model.addAttribute("user", user);
			return "classes";
		} else {
			return "classes";
		}
	}
	
	/* =============== DOCENTES =============== */
	@GetMapping("/team")
	public String team(Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			model.addAttribute("user", user);
			return "team";
		} else {
			return "team";
		}
	}
	
	/* =============== CONTACTO =============== */
	@GetMapping("/contact")
	public String contact(Message message, Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			model.addAttribute("message", message);
			model.addAttribute("user", user);
			return "contact";
		} else {
			return "contact";
		}
	}
	
	/* =============== LOGOUT =============== */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(request, null, null);
		return "redirect:/";
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
}
