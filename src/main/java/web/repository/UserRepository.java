package web.repository;

import web.model.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Integer>{
    
	/* ===== BUSCAR USUARIOS POR EMAIL ===== */
	Users findByEmail(String email);
	
	/* ===== VALIDAR TOKEN PARA QUE EL USUARIO RESETEE LA CLAVE ===== */
	public Users findByResetPasswordToken(String token);
	
	/* ===== BUSCAR USUARIOS POR CÓDIGO DE VERIFICACIÓN ===== */
	@Query("SELECT u FROM Users u WHERE u.verificationCode = ?1")
	public Users findByVerificationCode(String code);
}
