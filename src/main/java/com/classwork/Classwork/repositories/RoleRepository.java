package com.classwork.Classwork.repositories;

import com.classwork.Classwork.models.auth.ERole;
import com.classwork.Classwork.models.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);

}
