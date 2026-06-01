package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.dto.HorarioDisponivelDTO;
import com.centraldocorte.api.dto.HorarioFuncionamentoDTO;
import com.centraldocorte.api.services.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/barbearias")
@RequiredArgsConstructor
@Tag(name = "Horários", description = "Endpoints para gerenciamento de horários de funcionamento")
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping("/{id}/horarios")
    @Operation(summary = "Buscar horários de funcionamento",
            description = "Retorna os horários de funcionamento de uma barbearia para todos os dias da semana")
    public ResponseEntity<List<HorarioFuncionamentoDTO>> getHorarios(@PathVariable String id) {
        List<HorarioFuncionamento> horarios = horarioService.getHorariosByBarbearia(id);

        List<HorarioFuncionamentoDTO> horariosDTO = horarios.stream()
                .map(h -> new HorarioFuncionamentoDTO(
                        h.getId(),
                        h.getDia().name(),
                        h.getHoraAbertura() != null ? h.getHoraAbertura().toString() : null,
                        h.getHoraFechamento() != null ? h.getHoraFechamento().toString() : null,
                        h.getFechado()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(horariosDTO);
    }

    @PutMapping("/{id}/horarios")
    @PreAuthorize("hasRole('BARBEARIA_ADM')")
    @Operation(summary = "Atualizar horários de funcionamento",
            description = "Atualiza os horários de funcionamento de uma barbearia (apenas proprietário)")
    public ResponseEntity<List<HorarioFuncionamentoDTO>> updateHorarios(
      @PathVariable String id,
      @Valid @RequestBody List<HorarioFuncionamentoDTO> horarios) {

        List<HorarioFuncionamento> saved = horarioService.saveHorarios(id, horarios);

        List<HorarioFuncionamentoDTO> responseDTO = saved.stream()
                .map(h -> new HorarioFuncionamentoDTO(
                        h.getId(),
                        h.getDia().name(),
                        h.getHoraAbertura() != null ? h.getHoraAbertura().toString() : null,
                        h.getHoraFechamento() != null ? h.getHoraFechamento().toString() : null,
                        h.getFechado()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}/horarios-disponiveis")
    @Operation(summary = "Buscar horários disponíveis",
            description = "Retorna todos os horários disponíveis para agendamento em uma data específica")
    public ResponseEntity<List<HorarioDisponivelDTO>> getHorariosDisponiveis(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false, defaultValue = "30") Integer duracaoServico) {

        List<HorarioDisponivelDTO> horarios = horarioService.getHorariosDisponiveisParaAgendamento(
                id, data, duracaoServico);

        return ResponseEntity.ok(horarios);
    }
}