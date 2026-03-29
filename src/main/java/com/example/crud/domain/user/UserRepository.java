package com.example.crud.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {


    List<User> findAllByActiveTrue();

    // Busca usuários por role
    List<User> findByRole(UserRole role);

    // Busca usuário por email
    Optional<User> findByEmail(String email);

    // Verifica se existe usuário com determinado email
    boolean existsByEmail(String email);

    // Busca usuários ativos por role
    List<User> findByRoleAndActiveTrue(UserRole role);

    // Busca usuários por nome
    List<User> findByNameContainingIgnoreCase(String name);

    // Busca usuários por telefone
    Optional<User> findByTelefone(String telefone);
}