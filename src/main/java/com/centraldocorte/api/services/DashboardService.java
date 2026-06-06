package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.dto.DashboardMetricasDTO;
import com.centraldocorte.api.dto.GraficoDTO;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AgendamentoRepository agendamentoRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional(readOnly = true)
    public DashboardMetricasDTO getMetricas(String barbeariaId) {

        // Verificar se a barbearia existe
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));

        // Data atual
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDoMes = agora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime inicioDoMesAnterior = inicioDoMes.minusMonths(1);

        // Total de agendamentos (todos)
        Long totalAgendamentos = agendamentoRepository.countByBarbeariaId(barbeariaId);

        // Agendamentos do mês atual
        Long agendamentosMes = agendamentoRepository.countByBarbeariaIdAndDataHoraBetween(
                barbeariaId, inicioDoMes, agora);

        // Agendamentos confirmados e concluídos
        Long agendamentosConfirmados = agendamentoRepository.countByBarbeariaIdAndStatusIn(
                barbeariaId,
                List.of(StatusAgendamento.CONFIRMADO, StatusAgendamento.CONCLUIDO)
        );

        // Taxa de confirmação
        Double taxaConfirmacao = totalAgendamentos > 0
                ? (agendamentosConfirmados.doubleValue() / totalAgendamentos.doubleValue()) * 100
                : 0.0;

        // Faturamento do mês (soma dos preços dos serviços de agendamentos confirmados e concluídos)
        BigDecimal faturamentoMes = agendamentoRepository.sumPrecoServicoByBarbeariaIdAndStatusInAndDataHoraBetween(
                barbeariaId,
                List.of(StatusAgendamento.CONFIRMADO, StatusAgendamento.CONCLUIDO),
                inicioDoMes,
                agora
        );
        if (faturamentoMes == null) faturamentoMes = BigDecimal.ZERO;

        // Clientes atendidos (clientes distintos com agendamentos confirmados ou concluídos)
        Long clientesAtendidos = agendamentoRepository.countDistinctClientesByBarbeariaIdAndStatusIn(
                barbeariaId,
                List.of(StatusAgendamento.CONFIRMADO, StatusAgendamento.CONCLUIDO)
        );

        // Cancelamentos
        Long cancelamentos = agendamentoRepository.countByBarbeariaIdAndStatusIn(
                barbeariaId,
                List.of(StatusAgendamento.CANCELADO_PELO_CLIENTE, StatusAgendamento.CANCELADO_PELA_BARBEARIA)
        );

        // Serviços realizados (agendamentos concluídos)
        Long servicosRealizados = agendamentoRepository.countByBarbeariaIdAndStatus(
                barbeariaId, StatusAgendamento.CONCLUIDO
        );

        return DashboardMetricasDTO.builder()
                .totalAgendamentos(totalAgendamentos)
                .agendamentosMes(agendamentosMes)
                .taxaConfirmacao(Math.round(taxaConfirmacao * 10) / 10.0)
                .faturamentoMes(faturamentoMes)
                .clientesAtendidos(clientesAtendidos)
                .cancelamentos(cancelamentos)
                .servicosRealizados(servicosRealizados)
                .build();
    }

    @Transactional(readOnly = true)
    public GraficoDTO getAgendamentosPorPeriodo(String barbeariaId, String periodo) {

        List<String> labels = new ArrayList<>();
        List<Long> valores = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();

        switch (periodo.toLowerCase()) {
            case "semana":
                // Últimos 7 dias
                for (int i = 6; i >= 0; i--) {
                    LocalDate data = LocalDate.now().minusDays(i);
                    String label = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    labels.add(label);

                    LocalDateTime inicio = data.atStartOfDay();
                    LocalDateTime fim = data.plusDays(1).atStartOfDay();
                    Long count = agendamentoRepository.countByBarbeariaIdAndDataHoraBetween(
                            barbeariaId, inicio, fim);
                    valores.add(count);
                }
                break;

            case "ano":
                // Últimos 12 meses
                for (int i = 11; i >= 0; i--) {
                    LocalDate data = LocalDate.now().minusMonths(i);
                    String label = data.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    labels.add(label);

                    YearMonth yearMonth = YearMonth.from(data);
                    LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
                    LocalDateTime fim = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                    Long count = agendamentoRepository.countByBarbeariaIdAndDataHoraBetween(
                            barbeariaId, inicio, fim);
                    valores.add(count);
                }
                break;

            default: // "mes" - últimas 4 semanas
                for (int i = 3; i >= 0; i--) {
                    LocalDate inicioSemana = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                    LocalDate fimSemana = inicioSemana.plusDays(6);
                    String label = String.format("%dª semana", 4 - i);
                    labels.add(label);

                    LocalDateTime inicio = inicioSemana.atStartOfDay();
                    LocalDateTime fim = fimSemana.plusDays(1).atStartOfDay();
                    Long count = agendamentoRepository.countByBarbeariaIdAndDataHoraBetween(
                            barbeariaId, inicio, fim);
                    valores.add(count);
                }
                break;
        }

        return GraficoDTO.builder()
                .labels(labels)
                .valores(valores)
                .build();
    }
}