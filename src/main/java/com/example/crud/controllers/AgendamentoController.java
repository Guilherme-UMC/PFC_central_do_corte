package com.example.crud.controllers;


import com.example.crud.domain.models.Agendamento;
import com.example.crud.dto.AgendamentoRequestDTO;
import com.example.crud.dto.AgendamentoResponseDTO;
import com.example.crud.services.AgendamentoService;
import com.example.crud.services.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearer-auth")
public class AgendamentoController {
    private final AgendamentoService agendamentoService;
    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<Agendamento> criarAgendamento(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AgendamentoRequestDTO dto) {
        var user = usuarioService.findByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agendamentoService.criarAgendamento(user.getId(), dto));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> meusAgendamentos(
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = usuarioService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(agendamentoService.listarAgendamentosCliente(user.getId()));
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    public ResponseEntity<List<AgendamentoResponseDTO>> agendamentosBarbearia(@PathVariable String barbeariaId) {
        return ResponseEntity.ok(agendamentoService.listarAgendamentosBarbearia(barbeariaId));
    }

    @PatchMapping("/{agendamentoId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    public ResponseEntity<Agendamento> atualizarStatus(
            @PathVariable String agendamentoId,
            @RequestParam String status) {
        return ResponseEntity.ok(agendamentoService.atualizarStatus(agendamentoId, status));
    }
}