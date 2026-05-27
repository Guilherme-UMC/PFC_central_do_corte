package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.dto.BarbeariaRequestDTO;
import com.centraldocorte.api.dto.BarbeariaResponseDTO;
import com.centraldocorte.api.services.BarbeariaService;
import com.centraldocorte.api.dto.ViaCepResponseDTO;
import com.centraldocorte.api.services.ViaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;

@RestController
@RequestMapping("/barbearia")
@RequiredArgsConstructor
@Tag(name = "Barbearias", description = "Gerenciamento de barbearias")
public class BarbeariaController {

    private final BarbeariaService barbeariaService;
    private final ViaCepService viaCepService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Criar nova barbearia", description = "Cria uma barbearia vinculada ao usuário autenticado")
    public ResponseEntity<BarbeariaResponseDTO> criarBarbearia(
            @Valid @RequestBody BarbeariaRequestDTO request,
            Authentication authentication) {

        Usuario proprietario = (Usuario) authentication.getPrincipal();
        BarbeariaResponseDTO resposta = barbeariaService.criarBarbearia(request, proprietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Listar todas as barbearias ativas")
    public ResponseEntity<Page<BarbeariaResponseDTO>> listarBarbeariasAtivas(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(barbeariaService.listarBarbeariasAtivas(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar barbearia por ID")
    public ResponseEntity<BarbeariaResponseDTO> buscarBarbeariaPorId(@PathVariable String id) {
        return ResponseEntity.ok(barbeariaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar barbearias por nome")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarBarbeariasPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(barbeariaService.buscarPorNome(nome));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar minhas barbearias", description = "Lista barbearias do usuário autenticado")
    public ResponseEntity<List<BarbeariaResponseDTO>> listarMinhasBarbearias(Authentication authentication) {
        Usuario proprietario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(barbeariaService.buscarPorProprietario(proprietario));
    }

    @GetMapping("/owner/{proprietarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buscar barbearias por proprietário (apenas ADMIN)")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarBarbeariasPorProprietario(
            @PathVariable String proprietarioId) {

        return ResponseEntity.ok(barbeariaService.buscarPorIdProprietario(proprietarioId));
    }

    @GetMapping("/buscar-por-localizacao")
    @Operation(summary = "Buscar barbearias por cidade e UF")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarBarbeariasPorCidadeEUf(
            @RequestParam String cidade,
            @RequestParam String uf) {

        return ResponseEntity.ok(barbeariaService.buscarPorCidadeUf(cidade, uf));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') and @barbeariaSecurity.isOwner(#id, authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualizar barbearia")
    public ResponseEntity<BarbeariaResponseDTO> atualizarBarbearia(
            @PathVariable String id,
            @Valid @RequestBody BarbeariaRequestDTO request) {

        return ResponseEntity.ok(barbeariaService.atualizarBarbearia(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM') and @barbeariaSecurity.isOwner(#id, authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativar barbearia (soft delete)")
    public ResponseEntity<Void> desativarBarbearia(@PathVariable String id) {
        barbeariaService.desativarBarbearia(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ativar/Desativar barbearia (apenas ADMIN)")
    public ResponseEntity<Void> alternarStatusDaBarbearia(@PathVariable String id) {
        barbeariaService.alternarStatusBarbearia(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar-por-cep/{cep}")
    @Operation(summary = "Buscar barbearias por CEP",
            description = "Busca barbearias cadastradas no CEP informado.")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarBarbeariasPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(barbeariaService.buscarPorCep(cep));
    }

    @GetMapping("/buscar-por-cep/avancado/{cep}")
    @Operation(summary = "Busca avançada por CEP com fallback para cidade")
    public ResponseEntity<Map<String, Object>> buscarBarbeariasPorCepAvancado(@PathVariable String cep) {
        return ResponseEntity.ok(barbeariaService.buscarBarbeariasPorCepComFallback(cep, viaCepService));
    }

    @GetMapping("/buscar-cep/{cep}")
    @Operation(summary = "Buscar endereço por CEP")
    public ResponseEntity<ViaCepResponseDTO> buscarCep(@PathVariable String cep) {

        ViaCepResponseDTO endereco = viaCepService.buscarEnderecoPorCep(cep);

        return ResponseEntity.ok(endereco);
    }
}