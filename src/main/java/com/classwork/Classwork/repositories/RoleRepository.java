package com.classwork.Classwork.repositories;

import com.classwork.Classwork.models.auth.ERole;
import com.classwork.Classwork.models.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);

  @Query(value = "select count(*) from role", nativeQuery = true)
    int isRoleEmpty();

  @Query(value = "Insert into role (name) name values(:roleName) ",
          nativeQuery = true)
  Set<ERole> insertRoles(@Param("roleName") ERole role);
}
