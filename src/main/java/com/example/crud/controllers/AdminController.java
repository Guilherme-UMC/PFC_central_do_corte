package com.example.crud.controllers;

import com.example.crud.domain.user.UserRole;
import com.example.crud.dto.UserRequestDTO;
import com.example.crud.dto.UserResponseDTO;
import com.example.crud.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administrador", description = "Endpoints exclusivos para administradores")
public class AdminController {

    private final UserService userService;

    @Operation(summary = "Criar usuário", description = "Admin pode criar usuários com qualquer role")
    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @Operation(summary = "Criar administrador", description = "Cria um novo usuário com ROLE_ADMIN")
    @PostMapping("/users/admin")
    public ResponseEntity<UserResponseDTO> createAdmin(@Valid @RequestBody UserRequestDTO request) {
        request.setRole(UserRole.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }
}
