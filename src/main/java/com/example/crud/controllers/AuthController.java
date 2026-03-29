package com.example.crud.controllers;

import com.example.crud.domain.user.RequestUser;
import com.example.crud.domain.user.User;
import com.example.crud.domain.user.UserRepository;
import com.example.crud.domain.user.UserRole;
import com.example.crud.dto.LoginRequestDTO;
import com.example.crud.dto.LoginResponseDTO;
import com.example.crud.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequestDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
            Authentication auth = authenticationManager.authenticate(usernamePassword);

            var user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            var userEntity = userRepository.findByEmail(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = tokenService.generateToken(userEntity);

            return ResponseEntity.ok(new LoginResponseDTO(
                    token,
                    userEntity.getId(),
                    userEntity.getName(),
                    userEntity.getRole().name(),
                    "Login realizado com sucesso"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new LoginResponseDTO(null, null, null, null, "Email ou senha inválidos"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RequestUser data) {
        // Verifica se o email já existe
        if (userRepository.existsByEmail(data.email())) {
            return ResponseEntity.badRequest().body(new LoginResponseDTO(null, null, null, null, "Email já cadastrado"));
        }

        // Criptografa a senha antes de salvar
        String encryptedPassword = passwordEncoder.encode(data.password());

        User newUser = new User();
        newUser.setName(data.name());
        newUser.setEmail(data.email());
        newUser.setPassword(encryptedPassword);
        newUser.setTelefone(data.telefone());
        newUser.setRole(data.role() != null ? data.role() : UserRole.ROLE_CLIENTE);
        newUser.setActive(true);

        userRepository.save(newUser);

        return ResponseEntity.ok(new LoginResponseDTO(null, newUser.getId(), newUser.getName(), newUser.getRole().name(), "Usuário cadastrado com sucesso"));
    }
}