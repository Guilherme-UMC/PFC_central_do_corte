package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.dto.HorarioDisponivelDTO;
import com.centraldocorte.api.dto.HorarioFuncionamentoDTO;
import com.centraldocorte.api.services.HorarioService;
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
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping("/{id}/horarios")
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
    public ResponseEntity<List<HorarioFuncionamentoDTO>> updateHorarios(  // ← CORRIGIDO: retorna DTO
                                                                          @PathVariable String id,
                                                                          @Valid @RequestBody List<HorarioFuncionamentoDTO> horarios) {

        List<HorarioFuncionamento> saved = horarioService.saveHorarios(id, horarios);

        // Converter para DTO antes de retornar
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
    public ResponseEntity<List<HorarioDisponivelDTO>> getHorariosDisponiveis(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false, defaultValue = "30") Integer duracaoServico) {

        List<HorarioDisponivelDTO> horarios = horarioService.getHorariosDisponiveisParaAgendamento(
                id, data, duracaoServico);

        return ResponseEntity.ok(horarios);
    }
}