package com.centraldocorte.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricasDTO {
    private Long totalAgendamentos;
    private Long agendamentosMes;
    private Double taxaConfirmacao;
    private BigDecimal faturamentoMes;
    private Long clientesAtendidos;
    private Double mediaAvaliacao;
    private Long cancelamentos;
    private Long servicosRealizados;
}