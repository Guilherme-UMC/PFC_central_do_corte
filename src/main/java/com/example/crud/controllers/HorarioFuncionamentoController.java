package com.example.crud.controllers;

import com.example.crud.dto.HorarioFuncionamentoRequestDTO;
import com.example.crud.dto.HorarioFuncionamentoResponseDTO;
import com.example.crud.services.HorarioFuncionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-auth")
@Tag(name = "Horários de Funcionamento", description = "Endpoints para gerenciamento de horários de funcionamento das barbearias")
public class HorarioFuncionamentoController {

    private final HorarioFuncionamentoService horarioService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Criar horário de funcionamento para uma barbearia")
    public ResponseEntity<HorarioFuncionamentoResponseDTO> criarHorario(
            @PathVariable String barbeariaId,
            @Valid @RequestBody HorarioFuncionamentoRequestDTO dto) {

        HorarioFuncionamentoResponseDTO response = horarioService.criarHorario(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Listar todos os horários de funcionamento de uma barbearia")
    public ResponseEntity<List<HorarioFuncionamentoResponseDTO>> listarHorarios(
            @PathVariable String barbeariaId) {

        List<HorarioFuncionamentoResponseDTO> horarios = horarioService.listarHorarios(barbeariaId);
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/barbearia/{barbeariaId}/dia/{diaSemana}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Buscar horários de um dia específico da semana")
    public ResponseEntity<List<HorarioFuncionamentoResponseDTO>> buscarHorariosPorDia(
            @PathVariable String barbeariaId,
            @PathVariable String diaSemana) {

        DayOfWeek day = DayOfWeek.valueOf(diaSemana.toUpperCase());
        List<HorarioFuncionamentoResponseDTO> horarios = horarioService.buscarHorariosPorDia(barbeariaId, day);
        return ResponseEntity.ok(horarios);
    }

    @PutMapping("/{horarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Atualizar um horário de funcionamento")
    public ResponseEntity<HorarioFuncionamentoResponseDTO> atualizarHorario(
            @PathVariable String horarioId,
            @Valid @RequestBody HorarioFuncionamentoRequestDTO dto) {

        HorarioFuncionamentoResponseDTO response = horarioService.atualizarHorario(horarioId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{horarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Desativar um horário de funcionamento (soft delete)")
    public ResponseEntity<Void> desativarHorario(@PathVariable String horarioId) {
        horarioService.desativarHorario(horarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{horarioId}/ativar")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Reativar um horário de funcionamento")
    public ResponseEntity<Void> ativarHorario(@PathVariable String horarioId) {
        horarioService.ativarHorario(horarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{horarioId}/permanente")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Remover permanentemente um horário de funcionamento")
    public ResponseEntity<Void> removerHorarioPermanentemente(@PathVariable String horarioId) {
        horarioService.removerHorario(horarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/barbearia/{barbeariaId}/verificar")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Verificar se um horário específico está dentro do expediente")
    public ResponseEntity<Boolean> verificarHorarioFuncionamento(
            @PathVariable String barbeariaId,
            @RequestParam String diaSemana,
            @RequestParam String hora) {

        DayOfWeek day = DayOfWeek.valueOf(diaSemana.toUpperCase());
        LocalTime time = LocalTime.parse(hora);

        boolean valido = horarioService.isHorarioValido(barbeariaId, day, time);
        return ResponseEntity.ok(valido);
    }
}