package web.exception;

public class UserNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public UserNotFoundException(String email) {
		super("No se encontró un usuario registrado con el email proporcionado");
	}
}
