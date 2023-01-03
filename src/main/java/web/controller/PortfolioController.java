package web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import web.model.Portfolio;
import web.model.Users;
import web.service.CategoryService;
import web.service.PortfolioService;
import web.service.UserService;

@Controller
@RequestMapping("/portfolio")
public class PortfolioController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
    private PortfolioService portfolioService;
	
	@Autowired
    private CategoryService categoryService;
	
	private Authentication authentication;
	
	/* =============== GALERÍA COMPLETA =============== */
	@GetMapping("/gallery")
	public String portfolio(Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			model.addAttribute("user", user);
			
			List<Portfolio> works = portfolioService.findAll();
			model.addAttribute("artwork", works);
			
			return "portfolio/gallery";
		} else {
			return "portfolio/gallery";
		}
	}
	
	/* =============== GUARDAR TRABAJO NUEVO (GET) =============== */
	@GetMapping("/create")
	public String crear(Portfolio newArt, Model model) {
    	authentication = SecurityContextHolder.getContext().getAuthentication();
		Users user = userService.findByEmail(authentication.getName());
		
		model.addAttribute("newArt", newArt);
    	model.addAttribute("user", user);
		return "portfolio/create";
	}
	
	/* =============== GUARDAR TRABAJO NUEVO (POST) =============== */
	@PostMapping("/save")
	public String guardar(@Valid @ModelAttribute("portfolio") Portfolio art, Model model, HttpServletRequest request, @RequestParam("artPicture") MultipartFile multiPart, 
			RedirectAttributes attributes) throws UnsupportedEncodingException, MessagingException, IOException {
		
		model.addAttribute("categories", categoryService.findAll());
		
        if(!multiPart.isEmpty()) {
			String ruta = "c:/arte/images/portfolio/";
			String nombreImagen = FileManager.saveFile(multiPart, ruta);
			art.setImage(nombreImagen);
		} 
        
        portfolioService.save(art);
        
		return "redirect:/portfolio/gallery";
	}
	
	/* =============== ACTUALIZAR TRABAJO (GET) =============== */
	@GetMapping("/edit/{id}")
   	public String editar(@PathVariable("id") Integer id, Model model) {
       	authentication = SecurityContextHolder.getContext().getAuthentication();
   		Users user = userService.findByEmail(authentication.getName());
       	
   		Portfolio edit = portfolioService.findById(id);
   		model.addAttribute("edit", edit);
   		model.addAttribute("user", user);
   		return "portfolio/edit";
   	}
	
	/* =============== ACTUALIZAR TRABAJO (POST) =============== */
	@PostMapping("/update")
   	public String actualizar(@Valid @ModelAttribute("portfolio") Portfolio art, @RequestParam("artPicture") MultipartFile multiPart, 
   			RedirectAttributes attributes) throws UnsupportedEncodingException, MessagingException, IOException {
   		
		Portfolio existing = portfolioService.findById(art.getId());
		System.out.println("ID del trabajo encontrado: " + existing.getId());
		
       	if(!multiPart.isEmpty()) {
       		String ruta = "c:/arte/images/portfolio/";
   			String nombreImagen = FileManager.saveFile(multiPart, ruta);
   			
   			if(nombreImagen != null) {
   				art.setImage(nombreImagen);
   			} 
   		} else {
   			art.setImage(existing.getImage());
   		}
   		
        portfolioService.update(art);
   	    attributes.addFlashAttribute("msg", "Datos actualizados");
   	    System.out.println("Nombre de la imagen: " + art.getImage());
   	    return "redirect:/portfolio/gallery";
   	}
	
	/* =============== DETALLES DE UN TRABAJO =============== */
	@GetMapping("/details/{id}")
	public String portfolioDetails(@PathVariable("id") int id, Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			Users user = userService.findByEmail(authentication.getName());
			Portfolio art = portfolioService.findById(id);
			model.addAttribute("work", art);
			model.addAttribute("user", user);
			return "portfolio/details";
		} else {
			Portfolio art = portfolioService.findById(id);
			model.addAttribute("work", art);
			return "portfolio/details";
		}
	}
	
	/* =============== ELIMINAR =============== */
	@GetMapping("/delete")
	public String eliminar(@RequestParam("id") Integer[] id, RedirectAttributes attributes) {
		portfolioService.delete(id);	
        
		if(id.length == 1) {
        	attributes.addFlashAttribute("msg", "Registro eliminado");
            return "redirect:/portfolio/gallery";
        } else {
        	attributes.addFlashAttribute("msg", "Registros eliminados");
            return "redirect:/portfolio/gallery";        	
    	}
	}
	
	/* =============== FILTRAR TRABAJOS =============== */
	@GetMapping("/search")
	public String buscar(@ModelAttribute("search") Portfolio art, Model model) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		Users user = userService.findByEmail(authentication.getName());
		model.addAttribute("user", user);
		
		ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("category", ExampleMatcher.GenericPropertyMatchers.contains());
		Example<Portfolio> example = Example.of(art, matcher);
		List<Portfolio> lista = portfolioService.findByExample(example);
		model.addAttribute("artwork", lista);
		
		return "portfolio/gallery";
	}
	
	@ModelAttribute
	public void setGenericos(Model model) {
		Portfolio portfolioSearch = new Portfolio();
		portfolioSearch.reset();
		model.addAttribute("artwork", portfolioService.findAll());
		model.addAttribute("categories", categoryService.findAll());
		model.addAttribute("search", portfolioSearch);
	}
	
	@ModelAttribute
	public void setCategories(Model model) {
		model.addAttribute("categories", categoryService.findAll());
	}
	
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}
}
