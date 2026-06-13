package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.EmailConfirmacaoToken;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.EmailConfirmacaoTokenRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.LoginResponseDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import com.centraldocorte.api.dto.RegisterResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LogSistemaService logSistemaService;
    private final EmailConfirmacaoTokenRepository emailConfirmacaoTokenRepository;
    private final EmailService emailService;

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO request, HttpServletRequest httpRequest) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.email());

            if (usuarioOpt.isPresent() && !usuarioOpt.get().isEmailConfirmado()) {
                Map<String, Object> detalhes = new HashMap<>();
                detalhes.put("email", request.email());

                logSistemaService.registrarLog(
                        "LOGIN",
                        "FALHA_LOGIN_EMAIL_NAO_CONFIRMADO",
                        "Usuario",
                        null,
                        String.format("Tentativa de login com email não confirmado: %s", request.email()),
                        detalhes,
                        httpRequest
                );
                throw new BusinessException("E-mail não confirmado. Verifique sua caixa de entrada e confirme seu cadastro.");
            }

            var credenciais = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication autenticacao = authenticationManager.authenticate(credenciais);

            Usuario usuario = (Usuario) autenticacao.getPrincipal();
            String accessToken = tokenService.gerarAccessToken(usuario);
            String refreshToken = tokenService.gerarRefreshToken(usuario);

            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("email", usuario.getEmail());
            detalhes.put("role", usuario.getRole().name());

            logSistemaService.registrarLog(
                    "LOGIN",
                    "LOGIN",
                    "Usuario",
                    usuario.getId(),
                    String.format("Usuário %s (%s) fez login com sucesso", usuario.getEmail(), usuario.getRole().name()),
                    detalhes,
                    httpRequest
            );

            return new LoginResponseDTO(
                    accessToken,
                    refreshToken,
                    usuario.getId(),
                    usuario.getName(),
                    usuario.getRole().name(),
                    "Login realizado com sucesso"
            );
        } catch (BadCredentialsException e) {
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("email", request.email());

            logSistemaService.registrarLog(
                    "LOGIN",
                    "FALHA_LOGIN",
                    "Usuario",
                    null,
                    String.format("Tentativa de login falha para email: %s", request.email()),
                    detalhes,
                    httpRequest
            );
            throw new BusinessException("Email ou senha inválidos");
        }
    }

    @Transactional
    public RegisterResponseDTO registrarUsuario(RegisterRequestDTO request, UsuarioRole role, HttpServletRequest httpRequest) {
        validarEmailDisponivel(request.email());

        Usuario novoUsuario = criarUsuarioAPartirDoRequest(request, role);
        novoUsuario.setActive(false);
        novoUsuario.setEmailConfirmado(false);
        usuarioRepository.save(novoUsuario);

        String confirmacaoToken = UUID.randomUUID().toString();
        EmailConfirmacaoToken token = EmailConfirmacaoToken.builder()
                .token(confirmacaoToken)
                .usuario(novoUsuario)
                .dataExpiracao(LocalDateTime.now().plusHours(24))
                .utilizado(false)
                .build();
        emailConfirmacaoTokenRepository.save(token);

        emailService.enviarEmailConfirmacaoCadastro(novoUsuario.getEmail(), confirmacaoToken);

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("email", novoUsuario.getEmail());
        detalhes.put("role", novoUsuario.getRole().name());
        detalhes.put("telefone", novoUsuario.getTelefone());

        logSistemaService.registrarLog(
                "USUARIO",
                "CRIADO_AGUARDANDO_CONFIRMACAO",
                "Usuario",
                novoUsuario.getId(),
                String.format("Novo usuário criado aguardando confirmação: %s", novoUsuario.getEmail(), novoUsuario.getRole().name()),
                detalhes,
                httpRequest
        );

        return new RegisterResponseDTO(
                novoUsuario.getId(),
                novoUsuario.getName(),
                novoUsuario.getEmail(),
                novoUsuario.getRole().name(),
                "Usuário cadastrado! Enviamos um link de confirmação para seu e-mail."
        );
    }

    public LoginResponseDTO renovarToken(String refreshToken) {
        String email = tokenService.validarRefreshToken(refreshToken);
        if (email == null) {
            throw new BusinessException("Refresh token inválido ou expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (!usuario.isActive()) {
            throw new BusinessException("Usuário inativo");
        }

        String novoAccessToken = tokenService.gerarAccessToken(usuario);

        return new LoginResponseDTO(
                novoAccessToken,
                refreshToken,
                usuario.getId(),
                usuario.getName(),
                usuario.getRole().name(),
                "Token renovado com sucesso"
        );
    }

    public void registrarLogout(HttpServletRequest httpRequest) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            if (usuario != null) {
                Map<String, Object> detalhes = new HashMap<>();
                detalhes.put("email", usuario.getEmail());
                detalhes.put("role", usuario.getRole().name());

                logSistemaService.registrarLog(
                        "LOGOUT",
                        "LOGOUT",
                        "Usuario",
                        usuario.getId(),
                        String.format("Usuário %s fez logout", usuario.getEmail()),
                        detalhes,
                        httpRequest
                );
            }
        } catch (Exception e) {

        }
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado: " + email);
        }
    }

    @Transactional
    public void confirmarEmail(String token) {
        EmailConfirmacaoToken confirmacaoToken = emailConfirmacaoTokenRepository
                .findByTokenAndUtilizadoFalse(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou já utilizado"));

        if (confirmacaoToken.isExpirado()) {
            throw new BusinessException("Token expirado. Solicite um novo e-mail de confirmação.");
        }

        Usuario usuario = confirmacaoToken.getUsuario();
        usuario.setActive(true);
        usuario.setEmailConfirmado(true);
        confirmacaoToken.setUtilizado(true);

        usuarioRepository.save(usuario);
        emailConfirmacaoTokenRepository.save(confirmacaoToken);

        log.info("E-mail confirmado para usuário: {}", usuario.getEmail());
    }

    @Transactional
    public void reenviarEmailConfirmacao(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (usuario.isEmailConfirmado()) {
            throw new BusinessException("Este e-mail já foi confirmado.");
        }

        emailConfirmacaoTokenRepository.deleteByUsuarioId(usuario.getId());

        String novoToken = UUID.randomUUID().toString();
        EmailConfirmacaoToken token = EmailConfirmacaoToken.builder()
                .token(novoToken)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusHours(24))
                .utilizado(false)
                .build();
        emailConfirmacaoTokenRepository.save(token);

        emailService.enviarEmailConfirmacaoCadastro(usuario.getEmail(), novoToken);

        log.info("E-mail de confirmação reenviado para: {}", email);
    }

    private Usuario criarUsuarioAPartirDoRequest(RegisterRequestDTO request, UsuarioRole role) {
        Usuario usuario = new Usuario();
        usuario.setName(request.name());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setTelefone(request.telefone());
        usuario.setRole(role);
        usuario.setActive(true);
        return usuario;
    }
}