package com.example.crud.controllers;

import com.example.crud.domain.user.UserRole;
import com.example.crud.dto.UserRequestDTO;
import com.example.crud.dto.UserResponseDTO;
import com.example.crud.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Listar todos os usuários ativos")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Listar usuários por role")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @Operation(summary = "Buscar usuários por nome")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<List<UserResponseDTO>> searchUsersByName(@RequestParam String name) {
        return ResponseEntity.ok(userService.findByName(name));
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @Operation(summary = "Deletar usuário (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ativar/Desativar usuário")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable String id) {
        userService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Alterar senha do usuário")
    @PostMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @RequestBody Map<String, String> passwords) {
        userService.changePassword(id, passwords.get("oldPassword"), passwords.get("newPassword"));
        return ResponseEntity.ok().build();
    }
}