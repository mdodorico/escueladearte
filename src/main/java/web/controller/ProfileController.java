package web.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import web.file.FileManager;
import web.model.Users;
import web.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/personal")
	public String profile(Authentication authentication, Model model) {
		Users user = userService.findByEmail(authentication.getName());
		model.addAttribute("user", user);
		return "profile/personal";
	}
	
	@PostMapping("/save")
	public String guardar(@Valid @ModelAttribute("user") Users user, Authentication authentication, RedirectAttributes attributes, 
			Model model, @RequestParam("profilePicture") MultipartFile multiPart) throws IOException {
		
		Users existing = userService.findByEmail(user.getEmail());
    	user.setPassword(passwordEncoder.encode(user.getPassword()));
    	user.setEmail(authentication.getName());
    	
    	if(!multiPart.isEmpty()) {
			String ruta = "c:/portfolio/images/users/";
			String nombreImagen = FileManager.saveFile(multiPart, ruta);
			if(nombreImagen != null) {
				user.setPhoto(nombreImagen);
			} 
		} else {
			user.setPhoto(existing.getPhoto());
		}
    	
        userService.update(user);
        attributes.addFlashAttribute("msg", "Los cambios fueron guardados");
		return "redirect:/profile/personal";
	}
	
	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") Integer id, RedirectAttributes attributes) {
    	if(id == 1) {
    		attributes.addFlashAttribute("alert", "Por motivos de seguridad, este usuario no puede ser eliminado del registro");
    		return "redirect:/profile/personal";
    	} else {
    		userService.deleteProfile(id);	
    		return "redirect:/login";
    	}
	}
}
