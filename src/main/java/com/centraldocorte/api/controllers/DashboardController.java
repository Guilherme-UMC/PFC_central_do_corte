package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.DashboardMetricasDTO;
import com.centraldocorte.api.dto.GraficoDTO;
import com.centraldocorte.api.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para métricas e gráficos")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/barbearia/{barbeariaId}/metricas")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    @Operation(summary = "Buscar métricas da barbearia")
    public ResponseEntity<DashboardMetricasDTO> getMetricas(@PathVariable String barbeariaId) {
        DashboardMetricasDTO metricas = dashboardService.getMetricas(barbeariaId);
        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/barbearia/{barbeariaId}/agendamentos-periodo")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    @Operation(summary = "Buscar agendamentos por período")
    public ResponseEntity<GraficoDTO> getAgendamentosPorPeriodo(
            @PathVariable String barbeariaId,
            @RequestParam(defaultValue = "mes") String periodo) {
        GraficoDTO dados = dashboardService.getAgendamentosPorPeriodo(barbeariaId, periodo);
        return ResponseEntity.ok(dados);
    }
}