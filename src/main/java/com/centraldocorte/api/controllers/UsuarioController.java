package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.UsuarioRequestDTO;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.dto.UsuarioUpdateDTO;
import com.centraldocorte.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Listar todos os usuários (paginado)",
            description = "Retorna uma página de usuários com suporte a filtros")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodosUsuarios(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) UsuarioRole role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<UsuarioResponseDTO> page;

        if (search != null && !search.trim().isEmpty()) {
            // Tenta buscar primeiro por ID exato
            String searchTerm = search.trim();
            try {
                Usuario usuario = usuarioService.buscarUsuarioPorIdIncluindoInativos(searchTerm);
                // Se encontrou por ID, retorna uma página com um único resultado
                UsuarioResponseDTO dto = usuarioService.converterParaResponseDTO(usuario);
                page = new PageImpl<>(List.of(dto), pageable, 1);
            } catch (Exception e) {
                // Se não é um ID válido (formato UUID), busca por nome
                page = usuarioService.buscarPorNome(searchTerm, pageable);
            }
        } else if (role != null && ativo != null) {
            page = usuarioService.listarPorRoleEStatus(role, ativo, pageable);
        } else if (role != null) {
            page = usuarioService.buscarPorRole(role, pageable);
        } else if (ativo != null) {
            page = usuarioService.listarPorStatus(ativo, pageable);
        } else {
            page = usuarioService.listarTodos(pageable);
        }

        return ResponseEntity.ok(page);
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
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarioPorRole(@PathVariable UsuarioRole role, Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscarPorRole(role, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Buscar usuários por nome")
    public ResponseEntity<Page<UsuarioResponseDTO>> buscarUsuarioPorName(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscarPorNome(name, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') or #id == authentication.principal.id")
    @Operation(summary = "Atualizar dados do usuário")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable String id,
            @Valid @RequestBody UsuarioUpdateDTO request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request, httpRequest));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar role do usuário (apenas ADMIN)")
    public ResponseEntity<UsuarioResponseDTO> alterarRoleUsuario(
            @PathVariable String id,
            @RequestParam UsuarioRole role,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(usuarioService.alterarRoleUsuario(id, role, httpRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Excluir usuário (soft delete)")
    public ResponseEntity<Void> desativarUsuario(
            @PathVariable String id,
            HttpServletRequest httpRequest) {

        usuarioService.desativarUsuario(id, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Ativar/Desativar usuário")
    public ResponseEntity<Void> alternarStatusDoUsuario(
            @PathVariable String id,
            HttpServletRequest httpRequest) {

        usuarioService.alternarStatusDoUsuario(id, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("#id == authentication.principal.id")
    @Operation(summary = "Alterar senha do usuário")
    public ResponseEntity<Void> alterarSenhaDoUsuario(
            @PathVariable String id,
            @RequestBody Map<String, String> senhas,
            HttpServletRequest httpRequest) {

        usuarioService.alterarSenha(id, senhas.get("oldPassword"), senhas.get("newPassword"), httpRequest);
        return ResponseEntity.ok().build();
    }
}