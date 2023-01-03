package web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import web.model.Role;
import web.model.Users;
import web.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    
    private Collection<Role> userRoles;
    
    public CustomUserDetailsService(UserRepository userRepository, Collection<Role> userRoles) {
		this.userRepository = userRepository;
		this.userRoles = userRoles;
	}
    
    /* Al sobreescribir el método loadUserByUsername, decido qué es lo que programa toma como username.
     * En este caso, el username es el email del usuario */
	@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
    	Users user = userRepository.findByEmail(email);

        if (user != null && user.getEnabled() == true) {
        	return new org.springframework.security.core.userdetails.User(
        			user.getEmail(),
                    user.getPassword(),
                    mapRolesToAuthorities(user.getRole()));
        } else if (user != null && user.getEnabled() == false) {
        	throw new UsernameNotFoundException("Error: algún dato está mal");
        } else {
            throw new UsernameNotFoundException("Datos incorrectos");
        }
    }
	
    private Collection < ? extends GrantedAuthority> mapRolesToAuthorities(Role role) {
    	userRoles.add(role);
        Collection < ? extends GrantedAuthority> mapRoles = userRoles.stream()
                .map(userRoles -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        return mapRoles;
    }
}

