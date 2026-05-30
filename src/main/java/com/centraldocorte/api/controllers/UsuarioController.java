package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.UsuarioRequestDTO;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.dto.UsuarioUpdateDTO;
import com.centraldocorte.api.services.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name="bearerAuth")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Listar todos os usuários ativos")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodosUsuarios(){
        return ResponseEntity.ok(usuarioService.listarTodosAtivos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') or #id == authentication.principal.id")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable String id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Listar usuários por role")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarioPorRole(@PathVariable UsuarioRole role) {
        return ResponseEntity.ok(usuarioService.buscarPorRole(role));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Buscar usuários por nome")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarUsuarioPorName(@RequestParam String name) {
        return ResponseEntity.ok(usuarioService.buscarPorNome(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') or #id == authentication.principal.id")
    @Operation(summary = "Atualizar dados do usuário")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable String id,
            @Valid @RequestBody UsuarioUpdateDTO request) {

        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar role do usuário (apenas ADMIN)")
    public ResponseEntity<UsuarioResponseDTO> alterarRoleUsuario(
            @PathVariable String id,
            @RequestParam UsuarioRole role) {

        return ResponseEntity.ok(usuarioService.alterarRoleUsuario(id, role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário (soft delete)")
    public ResponseEntity<Void> desativarUsuario(@PathVariable String id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar/Desativar usuário")
    public ResponseEntity<Void> alternarStatusDoUsuario(@PathVariable String id) {
        usuarioService.alternarStatusDoUsuario(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Alterar senha do usuário")
    public ResponseEntity<Void> alterarSenhaDoUsuario(
            @PathVariable String id,
            @RequestBody Map<String, String> senhas) {

        usuarioService.alterarSenha(id, senhas.get("oldPassword"), senhas.get("newPassword"));
        return ResponseEntity.ok().build();
    }
}