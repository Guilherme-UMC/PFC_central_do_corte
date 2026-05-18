package com.centraldocorte.api.controllers;


import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.services.AgendamentoService;
import com.centraldocorte.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Agendamentos", description = "Endpoints para agendamentos")
public class AgendamentoController {
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<Agendamento> criarAgendamento(
            @AuthenticationPrincipal UserDetails usuarioAutenticado,
            @Valid @RequestBody AgendamentoRequestDTO dto) {

        String clienteId = usuarioService.buscarPorEmail(usuarioAutenticado.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoService.criarAgendamento(clienteId, dto));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarMeusAgendamentos(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        String clienteId = usuarioService.buscarPorEmail(usuarioAutenticado.getUsername()).getId();
        return ResponseEntity.ok(agendamentoService.listarAgendamentosCliente(clienteId));
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosBarbearia(
            @PathVariable String barbeariaId) {
        return ResponseEntity.ok(agendamentoService.listarAgendamentosBarbearia(barbeariaId));
    }

    @PatchMapping("/{agendamentoId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    public ResponseEntity<Agendamento> atualizarStatusAgendamento(
            @PathVariable String agendamentoId,
            @RequestParam String status) {
        return ResponseEntity.ok(agendamentoService.atualizarStatusAgendamento(agendamentoId, status));
    }
}