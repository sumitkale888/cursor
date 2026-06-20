package com.cursor.backend.repository;

import com.cursor.backend.entity.Role;
import com.cursor.backend.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
