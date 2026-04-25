package com.example.crud.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {


    List<User> findAllByActiveTrue();

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndActiveTrue(UserRole role);

    List<User> findByNameContainingIgnoreCase(String name);

    boolean existsByRoleAndActiveTrue(UserRole role);

}