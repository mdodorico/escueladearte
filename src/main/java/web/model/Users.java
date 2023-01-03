package web.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="users")
public class Users {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Getter @Setter Integer id;

    @Column(nullable=false)
    @NotEmpty
    private @Getter @Setter String firstName;
    
    @Column(nullable=false)
    @NotEmpty
    private @Getter @Setter String lastName;

    @Column(nullable=false, unique=true)
    @NotEmpty
    @Email
    private @Getter @Setter String email;

    @Column(nullable=false)
    @NotEmpty
    private @Getter @Setter String password;
    
    @Column(nullable=false)
    private @Getter @Setter Boolean enabled;
    
    @Column(name = "verification_code", length = 64)
    private @Getter @Setter String verificationCode;
    
    @Column(nullable=true)
    private @Getter @Setter String photo = "avatar.png";
    
    @Column(name = "reset_password_token")
    private @Getter @Setter String resetPasswordToken;
    
    @OneToOne(fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles", 
			joinColumns = @JoinColumn(name = "user_id"), 
			inverseJoinColumns = @JoinColumn(name = "role_id") 
	)
    private @Getter @Setter Role role;
    
    public String getFullName() {
		return this.getFirstName() + " " + this.getLastName();
	}

	@Override
	public String toString() {
		return "Usuario: id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", password=" + password + ", enabled=" + enabled + ", verificationCode=" + verificationCode
				+ ", photo=" + photo + ", role=" + role;
	}
}
