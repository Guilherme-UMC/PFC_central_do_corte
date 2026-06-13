package com.centraldocorte.api.services;
import com.centraldocorte.api.domain.models.RecuperacaoSenhaToken;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.RecuperacaoSenhaTokenRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final long TOKEN_VALIDADE_HORAS = 1;

    private final UsuarioRepository usuarioRepository;
    private final RecuperacaoSenhaTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email não encontrado"));

        if (!usuario.isActive()) {
            throw new BusinessException("Usuário inativo. Entre em contato com o suporte.");
        }

        if (!usuario.isEmailConfirmado()) {
            throw new BusinessException("E-mail não confirmado. Por favor, confirme seu e-mail antes de redefinir a senha.");
        }

        tokenRepository.deleteByUsuarioId(usuario.getId());

        String token = UUID.randomUUID().toString();
        RecuperacaoSenhaToken recuperacaoToken = new RecuperacaoSenhaToken();
        recuperacaoToken.setToken(token);
        recuperacaoToken.setUsuario(usuario);
        recuperacaoToken.setDataExpiracao(LocalDateTime.now().plusHours(TOKEN_VALIDADE_HORAS));
        recuperacaoToken.setUtilizado(false);

        tokenRepository.save(recuperacaoToken);

        emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), token);

        log.info("Solicitação de redefinição de senha para email: {}", email);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        RecuperacaoSenhaToken recuperacaoToken = tokenRepository
                .findByTokenAndUtilizadoFalse(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou já utilizado"));

        if (recuperacaoToken.isExpirado()) {
            throw new BusinessException("Token expirado. Solicite uma nova redefinição de senha.");
        }

        Usuario usuario = recuperacaoToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        recuperacaoToken.setUtilizado(true);

        usuarioRepository.save(usuario);
        tokenRepository.save(recuperacaoToken);

        log.info("Senha redefinida com sucesso para usuário: {}", usuario.getEmail());
    }

    public boolean validarToken(String token) {
        return tokenRepository.findByTokenAndUtilizadoFalse(token)
            .map(t -> !t.isExpirado())
            .orElse(false);
    }
}