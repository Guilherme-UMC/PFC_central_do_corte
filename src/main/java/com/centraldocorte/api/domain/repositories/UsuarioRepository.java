package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<Usuario> findAll(Pageable pageable);
    Page<Usuario> findByRole(UsuarioRole role, Pageable pageable);
    Page<Usuario> findByActive(boolean active, Pageable pageable);
    Page<Usuario> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Usuario> findByRoleAndActive(UsuarioRole role, boolean active, Pageable pageable);
    boolean existsByRoleAndActiveTrue(UsuarioRole role);
    long countByRoleAndActiveTrue(UsuarioRole role);
    List<Usuario> findByRoleAndActiveTrue(UsuarioRole role);
}