package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.RecuperacaoSenhaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecuperacaoSenhaTokenRepository extends JpaRepository<RecuperacaoSenhaToken, Long> {
    Optional<RecuperacaoSenhaToken> findByTokenAndUtilizadoFalse(String token);
    void deleteByUsuarioId(String usuarioId);
}