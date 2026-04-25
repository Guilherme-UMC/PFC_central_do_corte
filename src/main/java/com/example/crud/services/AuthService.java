package com.example.crud.services;

import com.example.crud.domain.user.User;
import com.example.crud.domain.user.UserRepository;
import com.example.crud.domain.user.UserRole;
import com.example.crud.dto.*;
import com.example.crud.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final BarbeariaService barbeariaService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
            Authentication auth = authenticationManager.authenticate(authToken);

            User user = (User) auth.getPrincipal();
            String token = tokenService.generateToken(user);

            return new LoginResponseDTO(
                    token,
                    user.getId(),
                    user.getName(),
                    user.getRole().name(),
                    "Login realizado com sucesso"
            );
        } catch (Exception e) {
            throw new RuntimeException("Email ou senha inválidos");
        }
    }

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request, UserRole forcedRole) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setTelefone(request.telefone());
        newUser.setRole(forcedRole);
        newUser.setActive(true);

        userRepository.save(newUser);

        return new RegisterResponseDTO(
                newUser.getId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getRole().name(),
                "Usuário cadastrado com sucesso"
        );
    }

    public LoginResponseDTO refreshToken(String oldToken) {
        String email = tokenService.validateToken(oldToken);
        if (email == null) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String newToken = tokenService.generateToken(user);

        return new LoginResponseDTO(
                newToken,
                user.getId(),
                user.getName(),
                user.getRole().name(),
                "Token renovado com sucesso"
        );
    }

}
