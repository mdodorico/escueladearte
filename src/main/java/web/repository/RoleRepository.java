package web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import web.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	Role findByName(String name);
}
