package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.dto.ServicoDTO;
import com.centraldocorte.api.dto.ServicoResponseDTO;
import com.centraldocorte.api.services.ServicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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