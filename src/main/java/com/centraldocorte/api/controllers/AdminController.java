package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.UsuarioRequestDTO;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UsuarioService usuarioService;

    @Operation(summary = "Criar usuário", description = "Admin pode criar usuários com qualquer role")
    @PostMapping("/users")
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(
            @Valid @RequestBody UsuarioRequestDTO request,
            HttpServletRequest httpRequest) {
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase().trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarUsuario(request, httpRequest));
    }

    @Operation(summary = "Criar administrador", description = "Cria um novo usuário com ROLE_ADMIN")
    @PostMapping("/users/admin")
    public ResponseEntity<UsuarioResponseDTO> criarAdmin(
            @Valid @RequestBody UsuarioRequestDTO request,
            HttpServletRequest httpRequest) {
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase().trim());
        }
        request.setRole(UsuarioRole.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarUsuario(request, httpRequest));
    }
}