package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.models.UsuarioRole;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.LoginResponseDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import com.centraldocorte.api.dto.RegisterResponseDTO;
import com.centraldocorte.api.dto.*;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponseDTO autenticarUsuario(LoginRequestDTO request) {
        try {
            var credenciais = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication autenticacao = authenticationManager.authenticate(credenciais);

            Usuario usuario = (Usuario) autenticacao.getPrincipal();
            String token = tokenService.gerarToken(usuario);

            return new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getName(),
                    usuario.getRole().name(),
                    "Login realizado com sucesso"
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException("Email ou senha inválidos");
        }
    }

    @Transactional
    public RegisterResponseDTO registrarUsuario(RegisterRequestDTO request, UsuarioRole role) {
        validarEmailDisponivel(request.email());

        Usuario novoUsuario = criarUsuarioAPartirDoRequest(request, role);
        usuarioRepository.save(novoUsuario);

        return new RegisterResponseDTO(
                novoUsuario.getId(),
                novoUsuario.getName(),
                novoUsuario.getEmail(),
                novoUsuario.getRole().name(),
                "Usuário cadastrado com sucesso"
        );
    }

    public LoginResponseDTO renovarToken(String tokenAntigo) {
        String email = tokenService.validarToken(tokenAntigo);
        if (email == null) {
            throw new BusinessException("Token inválido ou expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String novoToken = tokenService.gerarToken(usuario);

        return new LoginResponseDTO(
                novoToken,
                usuario.getId(),
                usuario.getName(),
                usuario.getRole().name(),
                "Token renovado com sucesso"
        );
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado: " + email);
        }
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