package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.LogSistemaFiltroDTO;
import com.centraldocorte.api.dto.LogSistemaResponseDTO;
import com.centraldocorte.api.services.LogSistemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Logs do Sistema", description = "Endpoints para visualização de logs (apenas ADMIN)")
public class LogSistemaController {

    private final LogSistemaService logService;

    @GetMapping
    @Operation(summary = "Buscar logs com filtros")
    public ResponseEntity<LogSistemaResponseDTO> buscarLogs(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LogSistemaFiltroDTO filtro = LogSistemaFiltroDTO.builder()
                .tipo(tipo)
                .acao(acao)
                .usuarioId(usuarioId)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .build();

        return ResponseEntity.ok(logService.buscarLogs(filtro, page, size));
    }

    @GetMapping("/tipos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar tipos de log disponíveis")
    public ResponseEntity<List<String>> getTipos() {
        return ResponseEntity.ok(logService.getTiposDisponiveis());
    }

    @GetMapping("/acoes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar ações disponíveis")
    public ResponseEntity<List<String>> getAcoes() {
        return ResponseEntity.ok(logService.getAcoesDisponiveis());
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter estatísticas dos logs")
    public ResponseEntity<Map<String, Long>> getEstatisticas() {
        return ResponseEntity.ok(logService.getEstatisticas());
    }
}