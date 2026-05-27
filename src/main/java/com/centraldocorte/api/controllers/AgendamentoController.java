package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.services.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(@Valid @RequestBody AgendamentoRequestDTO request) {
        AgendamentoResponseDTO agendamento = agendamentoService.criarAgendamento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEARIA_ADM')")
    public ResponseEntity<AgendamentoResponseDTO> cancelarAgendamento(
            @PathVariable Long id,
            @RequestParam String motivo) {
        AgendamentoResponseDTO agendamento = agendamentoService.cancelarAgendamento(id, motivo);
        return ResponseEntity.ok(agendamento);
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    public ResponseEntity<AgendamentoResponseDTO> confirmarAgendamento(@PathVariable Long id) {
        AgendamentoResponseDTO agendamento = agendamentoService.confirmarAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }

    @PutMapping("/{id}/concluir")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    public ResponseEntity<AgendamentoResponseDTO> concluirAgendamento(@PathVariable Long id) {
        AgendamentoResponseDTO agendamento = agendamentoService.concluirAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }

    @GetMapping("/cliente/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> getMeusAgendamentos() {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoCliente();
        return ResponseEntity.ok(agendamentos);
    }

    // CORRIGIDO: Long → String
    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAgendamentosBarbearia(@PathVariable String barbeariaId) {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDaBarbearia(barbeariaId);
        return ResponseEntity.ok(agendamentos);
    }

    // CORRIGIDO: Long → String
    @GetMapping("/barbearia/{barbeariaId}/hoje")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAgendamentosHoje(@PathVariable String barbeariaId) {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoDia(barbeariaId);
        return ResponseEntity.ok(agendamentos);
    }
}