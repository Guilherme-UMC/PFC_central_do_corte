// domain/repositories/EmailConfirmacaoTokenRepository.java
package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.EmailConfirmacaoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface EmailConfirmacaoTokenRepository extends JpaRepository<EmailConfirmacaoToken, String> {

    /**
     * Busca token não utilizado por valor do token
     */
    Optional<EmailConfirmacaoToken> findByTokenAndUtilizadoFalse(String token);

    /**
     * Busca token por ID do usuário
     */
    Optional<EmailConfirmacaoToken> findByUsuarioId(String usuarioId);

    /**
     * Verifica se existe token válido (não utilizado e não expirado) para o usuário
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM EmailConfirmacaoToken t " +
            "WHERE t.usuario.id = :usuarioId AND t.utilizado = false AND t.dataExpiracao > CURRENT_TIMESTAMP")
    boolean existsValidTokenByUsuarioId(@Param("usuarioId") String usuarioId);

    /**
     * Remove todos os tokens de um usuário (útil ao reenviar confirmação)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailConfirmacaoToken t WHERE t.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") String usuarioId);

    /**
     * Remove tokens expirados (pode ser usado em job agendado)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailConfirmacaoToken t WHERE t.dataExpiracao < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}