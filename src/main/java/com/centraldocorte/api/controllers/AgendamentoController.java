package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.services.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Agendamento", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Criar novo agendamento", description = "Cria um novo agendamento para o cliente autenticado")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento(@Valid @RequestBody AgendamentoRequestDTO request) {
        AgendamentoResponseDTO agendamento = agendamentoService.criarAgendamento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'BARBEARIA_ADM')")
    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento existente (cliente ou dono da barbearia)")
    public ResponseEntity<AgendamentoResponseDTO> cancelarAgendamento(
            @PathVariable Long id,
            @RequestParam String motivo) {
        AgendamentoResponseDTO agendamento = agendamentoService.cancelarAgendamento(id, motivo);
        return ResponseEntity.ok(agendamento);
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('BARBEARIA_ADM', 'FUNCIONARIO')")
    @Operation(summary = "Confirmar agendamento", description = "Confirma um agendamento pendente (apenas dono da barbearia)")
    public ResponseEntity<AgendamentoResponseDTO> confirmarAgendamento(@PathVariable Long id) {
        AgendamentoResponseDTO agendamento = agendamentoService.confirmarAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }

    @PutMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('BARBEARIA_ADM', 'FUNCIONARIO')")  // Adicione FUNCIONARIO
    @Operation(summary = "Concluir agendamento", description = "Marca um agendamento como concluído")
    public ResponseEntity<AgendamentoResponseDTO> concluirAgendamento(@PathVariable Long id) {
        AgendamentoResponseDTO agendamento = agendamentoService.concluirAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }

    @GetMapping("/cliente/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar meus agendamentos (cliente)", description = "Lista todos os agendamentos do cliente autenticado")
    public ResponseEntity<List<AgendamentoResponseDTO>> getMeusAgendamentos() {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoCliente();
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    @Operation(summary = "Listar agendamentos da barbearia", description = "Lista todos os agendamentos de uma barbearia (apenas dono ou admin)")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAgendamentosBarbearia(@PathVariable String barbeariaId) {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDaBarbearia(barbeariaId);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/barbearia/{barbeariaId}/hoje")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    @Operation(summary = "Listar agendamentos do dia", description = "Lista os agendamentos do dia atual para uma barbearia")
    public ResponseEntity<List<AgendamentoResponseDTO>> getAgendamentosHoje(@PathVariable String barbeariaId) {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoDia(barbeariaId);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/funcionario/meus")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @Operation(summary = "Listar meus agendamentos (funcionário)", description = "Lista todos os agendamentos do funcionário autenticado")
    public ResponseEntity<List<AgendamentoResponseDTO>> getMeusAgendamentosComoFuncionario() {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoFuncionario();
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping("/funcionario/hoje")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @Operation(summary = "Listar meus agendamentos do dia (funcionário)", description = "Lista os agendamentos do dia atual do funcionário autenticado")
    public ResponseEntity<List<AgendamentoResponseDTO>> getMeusAgendamentosHojeComoFuncionario() {
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.getAgendamentosDoDiaDoFuncionario();
        return ResponseEntity.ok(agendamentos);
    }
}