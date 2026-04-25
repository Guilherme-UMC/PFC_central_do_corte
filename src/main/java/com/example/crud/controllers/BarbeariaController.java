package com.example.crud.controllers;

import com.example.crud.domain.user.User;
import com.example.crud.dto.BarbeariaRequestDTO;
import com.example.crud.dto.BarbeariaResponseDTO;
import com.example.crud.services.BarbeariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbearia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Barbearias", description = "Gerenciamento de barbearias")
public class BarbeariaController {

    private final BarbeariaService barbeariaService;

    @Operation(summary = "Criar nova barbearia", description = "Cria uma barbearia vinculada ao usuário autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<BarbeariaResponseDTO> criar(
            @Valid @RequestBody BarbeariaRequestDTO request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        BarbeariaResponseDTO response = barbeariaService.criar(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todas as barbearias ativas")
    @GetMapping
    public ResponseEntity<Page<BarbeariaResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(barbeariaService.listarTodos(pageable));
    }

    @Operation(summary = "Buscar barbearia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<BarbeariaResponseDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(barbeariaService.buscarPorId(id));
    }

    @Operation(summary = "Buscar barbearias por nome")
    @GetMapping("/buscar")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(barbeariaService.buscarPorNome(nome));
    }

    @Operation(summary = "Listar minhas barbearias", description = "Lista barbearias do usuário autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my-barbearias")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarMinhasBarbearias(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(barbeariaService.buscarPorOwner(currentUser));
    }

    @Operation(summary = "Buscar barbearias por proprietário", description = "Apenas ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarPorOwnerId(@PathVariable String ownerId) {
        return ResponseEntity.ok(barbeariaService.buscarPorOwnerId(ownerId));
    }

    @Operation(summary = "Buscar barbearias por localização")
    @GetMapping("/buscar-por-localizacao")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarPorCidadeUf(
            @RequestParam String cidade,
            @RequestParam String uf) {
        return ResponseEntity.ok(barbeariaService.buscarPorCidadeUf(cidade, uf));
    }

    @Operation(summary = "Atualizar barbearia")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') and @barbeariaSecurity.isOwner(#id, authentication)")
    public ResponseEntity<BarbeariaResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody BarbeariaRequestDTO request) {
        return ResponseEntity.ok(barbeariaService.atualizar(id, request));
    }

    @Operation(summary = "Deletar barbearia (soft delete)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') and @barbeariaSecurity.isOwner(#id, authentication)")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        barbeariaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ativar/Desativar barbearia")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ativarDesativar(@PathVariable String id) {
        barbeariaService.ativarDesativar(id);
        return ResponseEntity.ok().build();
    }
}