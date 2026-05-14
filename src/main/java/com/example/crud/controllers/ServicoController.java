package com.example.crud.controllers;

import com.example.crud.domain.models.Servico;
import com.example.crud.dto.ServicoDTO;
import com.example.crud.dto.ServicoResponseDTO;
import com.example.crud.services.ServicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-auth")
public class ServicoController {
    private final ServicoService servicoService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<ServicoResponseDTO> criarServico(
            @PathVariable String barbeariaId,
            @Valid @RequestBody ServicoDTO dto) {

        Servico servico = servicoService.criarServico(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponseDTO(servico));
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    public ResponseEntity<List<ServicoResponseDTO>> listarServicos(@PathVariable String barbeariaId) {
        List<ServicoResponseDTO> servicos = servicoService.listarServicosPorBarbearia(barbeariaId)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
        return ResponseEntity.ok(servicos);
    }

    @PutMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<ServicoResponseDTO> atualizarServico(
            @PathVariable String servicoId,
            @Valid @RequestBody ServicoDTO dto) {

        Servico servico = servicoService.atualizarServico(servicoId, dto);
        return ResponseEntity.ok(convertToResponseDTO(servico));
    }

    @DeleteMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    public ResponseEntity<Void> desativarServico(@PathVariable String servicoId) {
        servicoService.desativarServico(servicoId);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para conversão
    private ServicoResponseDTO convertToResponseDTO(Servico servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());
        dto.setAtivo(servico.getAtivo());
        dto.setBarbeariaId(servico.getBarbearia().getId());
        dto.setBarbeariaNome(servico.getBarbearia().getNome());
        return dto;
    }
}