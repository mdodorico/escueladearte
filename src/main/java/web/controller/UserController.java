package web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import web.file.FileManager;
import web.model.Role;
import web.model.Users;
import web.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	private Authentication authentication;
    
	@GetMapping("/list")
        public String listRegisteredUsers(Model model){
		List<Users> users = userService.findAllUsers();
		authentication = SecurityContextHolder.getContext().getAuthentication();
		Users user = userService.findByEmail(authentication.getName());
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        return "users/list";
    }

    @ModelAttribute
	public void setRoles(Model model) {
		model.addAttribute("roles", userService.findAllRoles());
	}
    
    @GetMapping("/create")
	public String crear(Users newUser, Model model) {
    	authentication = SecurityContextHolder.getContext().getAuthentication();
		Users user = userService.findByEmail(authentication.getName());
		
		model.addAttribute("newUser", newUser);
    	model.addAttribute("user", user);
		return "users/create";
	}
    
    @PostMapping("/save")
	public String guardar(@Valid @ModelAttribute("user") Users user, HttpServletRequest request, @RequestParam("profilePicture") MultipartFile multiPart, 
			RedirectAttributes attributes) throws UnsupportedEncodingException, MessagingException, IOException {
		
    	Users existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            attributes.addFlashAttribute("alert", "El email ya se encuentra registrado");
            return "redirect:/users/list";
        }
        
        if(!multiPart.isEmpty()) {
			String ruta = "c:/arte/images/users/";
			String nombreImagen = FileManager.saveFile(multiPart, ruta);
			if(nombreImagen != null) {
				user.setPhoto(nombreImagen);
			}
		} 
        
        userService.save(user, getSiteURL(request));
        attributes.addFlashAttribute("msg", "Se envió un email al usuario para verificar su cuenta.");
		return "redirect:/users/list";
	}
    
    private String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}
    
   
    @PostMapping("/update")
   	public String actualizar(@Valid @ModelAttribute("user") Users user, @RequestParam("profilePicture") MultipartFile multiPart, 
   			RedirectAttributes attributes) throws UnsupportedEncodingException, MessagingException, IOException {
   		
       	Users existing = userService.findByEmail(user.getEmail());
       	
       	if(!multiPart.isEmpty()) {
   			String ruta = "c:/arte/images/users/";
   			String nombreImagen = FileManager.saveFile(multiPart, ruta);
   			if(nombreImagen != null) {
   				user.setPhoto(nombreImagen);
   			} 
   		} else {
   			user.setPhoto(existing.getPhoto());
   		}
       	
       	user.setPassword(passwordEncoder.encode(user.getPassword()));
       	
       	Role role = (Role) user.getRole();
   		role.setId(role.getId());
   		user.setRole(role);
   		
   		userService.update(user);
   	    attributes.addFlashAttribute("msg", "Datos actualizados");
   	    return "redirect:/users/list";
   	}
    
    @GetMapping("/sendEmail/{id}")
   	public String reenviarEmail(@PathVariable("id") Integer id, HttpServletRequest request, RedirectAttributes attributes) throws MessagingException, UnsupportedEncodingException{
    	Users user = userService.findById(id);
    	if(!user.getEnabled()) {
    		userService.resendEmail(user, getSiteURL(request));
    		attributes.addFlashAttribute("email", "Se reenvió el email de verificación");
    		return "redirect:/users/list";
    	} else {
    		attributes.addFlashAttribute("error", "ERROR: no se pudo reenviar el email porque la cuenta ya está verificada");
    		return "redirect:/users/list";
    	}
   	}
    
    @GetMapping("/edit/{id}")
   	public String editar(@PathVariable("id") Integer id, Model model) {
       	authentication = SecurityContextHolder.getContext().getAuthentication();
   		Users user = userService.findByEmail(authentication.getName());
       	
   		Users edit = userService.findById(id);
   		model.addAttribute("edit", edit);
   		model.addAttribute("user", user);
   		return "users/edit";
   	}
       
    @GetMapping("/verify")
	public String verifyUser(@Param("code") String code) {
		if (userService.verify(code)) {
			return "register/verify_success";
		} else {
			return "register/verify_fail";
		}
	}
    
    @GetMapping("/delete")
	public String eliminar(@RequestParam("id") Integer[] id, RedirectAttributes attributes) {
    	for(Integer idUser: id) {
    		if(idUser == 1) {
        		attributes.addFlashAttribute("alert", "Por motivos de seguridad, el usuario con id=1 no puede ser eliminado del registro");
        		return "redirect:/users/list";
        	} else {
        		userService.delete(id);	
        		if(id.length == 1) {
        			attributes.addFlashAttribute("msg", "Registro eliminado");
            		return "redirect:/users/list";
        		} else {
        			attributes.addFlashAttribute("msg", "Registros eliminados");
            		return "redirect:/users/list";
        		}
        	}
    	}
    	return null;
	}
    
    @GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") Integer id, RedirectAttributes attributes) {
    	if(id == 1) {
    		attributes.addFlashAttribute("alert", "Por motivos de seguridad, este usuario no puede ser eliminado del registro");
    		return "redirect:/users/list";
    	} else {
    		userService.deleteProfile(id);	
    		attributes.addFlashAttribute("msg", "Registro eliminado");
    		return "redirect:/users/list";
    	}
	}
    
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}
}
