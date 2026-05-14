package com.example.crud.domain.repositories;

import com.example.crud.domain.models.User;
import com.example.crud.domain.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByActiveTrue();
    List<User> findByRoleAndActiveTrue(UserRole role);
    List<User> findByNameContainingIgnoreCase(String name);
    boolean existsByRoleAndActiveTrue(UserRole role);
}