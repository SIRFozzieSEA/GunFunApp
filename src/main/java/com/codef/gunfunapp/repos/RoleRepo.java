package com.codef.gunfunapp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunapp.models.entities.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

	Role findByRole(String role);

}
