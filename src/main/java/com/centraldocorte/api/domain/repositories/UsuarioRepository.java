package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.UsuarioRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findAllByActiveTrue();
    List<Usuario> findByRoleAndActiveTrue(UsuarioRole role);
    List<Usuario> findByNameContainingIgnoreCase(String name);
    boolean existsByRoleAndActiveTrue(UsuarioRole role);
}