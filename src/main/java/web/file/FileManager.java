package web.file;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public class FileManager {
    	
    public static String saveFile(MultipartFile multiPart, String path) {
		
		String nombreOriginal = multiPart.getOriginalFilename();
		nombreOriginal = nombreOriginal.replace(" ", "-");
		String nombreFinal = randomAlphanumeric(8) + nombreOriginal;
		
		try {
			File imageFile = new File(path + nombreFinal);
			multiPart.transferTo(imageFile);
			return nombreFinal;
		} catch (IOException e) {
			System.out.println("Error " + e.getMessage());
			return null;
		}
	}
	
	public static String randomAlphanumeric(int count) {
		String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder builder = new StringBuilder();
		while(count-- != 0) {
			int character = (int)(Math.random() * CARACTERES.length());
			builder.append(CARACTERES.charAt(character));
		}
		return builder.toString();
	}
}
