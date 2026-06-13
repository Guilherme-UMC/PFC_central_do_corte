package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.enums.CategoriaServico;
import com.centraldocorte.api.dto.BarbeariaResponseDTO;
import com.centraldocorte.api.dto.ServicoDTO;
import com.centraldocorte.api.dto.ServicoResponseDTO;
import com.centraldocorte.api.services.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços das barbearias")
public class ServicoController {
    private final ServicoService servicoService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<ServicoResponseDTO> criarServico(
            @PathVariable String barbeariaId,
            @Valid @RequestBody ServicoDTO dto) {

        ServicoResponseDTO servico = servicoService.criarServico(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servico);
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ServicoResponseDTO>> listarServicosBarbearia(@PathVariable String barbeariaId) {
        List<ServicoResponseDTO> servicos = servicoService.listarServicosAtivosBarbearia(barbeariaId);
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/barbearia/{barbeariaId}/categoria/{categoria}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ServicoResponseDTO>> listarServicosPorCategoria(
            @PathVariable String barbeariaId,
            @PathVariable String categoria) {
        List<ServicoResponseDTO> servicos = servicoService.listarServicosPorCategoria(barbeariaId, categoria);
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/barbearia/{barbeariaId}/categorias")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> listarCategoriasDaBarbearia(@PathVariable String barbeariaId) {
        List<CategoriaServico> categorias = servicoService.listarCategoriasDisponiveis(barbeariaId);
        List<String> nomesCategorias = categorias.stream()
                .map(CategoriaServico::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(nomesCategorias);
    }

    @GetMapping("/categorias")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> listarTodasCategorias() {
        return ResponseEntity.ok(CategoriaServico.getNomes());
    }

    @GetMapping("/buscar-barbearias")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Buscar barbearias por categoria de serviço")
    public ResponseEntity<List<BarbeariaResponseDTO>> buscarBarbeariasPorCategoriaServico(
            @RequestParam String categoria) {
        List<BarbeariaResponseDTO> barbearias = servicoService.buscarBarbeariasPorCategoriaServicoDTO(categoria);
        return ResponseEntity.ok(barbearias);
    }

    @PutMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<ServicoResponseDTO> atualizarServico(
            @PathVariable String servicoId,
            @Valid @RequestBody ServicoDTO dto) {

        ServicoResponseDTO servico = servicoService.atualizarServico(servicoId, dto);
        return ResponseEntity.ok(servico);
    }

    @DeleteMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<Void> desativarServico(@PathVariable String servicoId) {
        servicoService.desativarServico(servicoId);
        return ResponseEntity.noContent().build();
    }
}