package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.FaturamentoMensalRepository;
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
    private final FaturamentoMensalRepository faturamentoMensalRepository;

    @Transactional(readOnly = true)
    public DashboardMetricasDTO getMetricas(String barbeariaId) {

        // Verificar se a barbearia existe
        barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDoMes = agora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime inicioDoAno = agora.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);

        // Total de agendamentos
        Long totalAgendamentos = agendamentoRepository.countByBarbeariaId(barbeariaId);
        if (totalAgendamentos == null) totalAgendamentos = 0L;

        // Agendamentos do mês atual
        Long agendamentosMes = agendamentoRepository.countByBarbeariaIdAndDataHoraBetween(
                barbeariaId, inicioDoMes, agora);
        if (agendamentosMes == null) agendamentosMes = 0L;

        // Agendamentos CONCLUÍDOS no total
        Long agendamentosConcluidos = agendamentoRepository.countByBarbeariaIdAndStatus(
                barbeariaId, StatusAgendamento.CONCLUIDO);
        if (agendamentosConcluidos == null) agendamentosConcluidos = 0L;

        // Taxa de conclusão
        Double taxaConclusao = totalAgendamentos > 0
                ? (agendamentosConcluidos.doubleValue() / totalAgendamentos.doubleValue()) * 100
                : 0.0;

        // FATURAMENTO DO MÊS (da tabela de resumo)
        Integer anoAtual = agora.getYear();
        Integer mesAtual = agora.getMonthValue();

        BigDecimal faturamentoMes = faturamentoMensalRepository.findValorTotalByBarbeariaAndMes(
                barbeariaId, anoAtual, mesAtual);
        if (faturamentoMes == null) faturamentoMes = BigDecimal.ZERO;

        // FATURAMENTO DO ANO
        BigDecimal faturamentoAno = faturamentoMensalRepository.findTotalFaturamentoAno(barbeariaId, anoAtual);
        if (faturamentoAno == null) faturamentoAno = BigDecimal.ZERO;

        // Clientes atendidos (clientes distintos com agendamentos CONCLUÍDOS)
        Long clientesAtendidos = agendamentoRepository.countDistinctClientesByBarbeariaIdAndStatus(
                barbeariaId, StatusAgendamento.CONCLUIDO);
        if (clientesAtendidos == null) clientesAtendidos = 0L;

        // Cancelamentos
        Long cancelamentos = agendamentoRepository.countByBarbeariaIdAndStatusIn(
                barbeariaId,
                List.of(StatusAgendamento.CANCELADO_PELO_CLIENTE, StatusAgendamento.CANCELADO_PELA_BARBEARIA)
        );
        if (cancelamentos == null) cancelamentos = 0L;

        // Serviços realizados (agendamentos CONCLUÍDOS)
        Long servicosRealizados = agendamentoRepository.countByBarbeariaIdAndStatus(
                barbeariaId, StatusAgendamento.CONCLUIDO);
        if (servicosRealizados == null) servicosRealizados = 0L;

        return DashboardMetricasDTO.builder()
                .totalAgendamentos(totalAgendamentos)
                .agendamentosMes(agendamentosMes)
                .taxaConfirmacao(Math.round(taxaConclusao * 10) / 10.0)
                .faturamentoMes(faturamentoMes)
                .faturamentoAno(faturamentoAno)
                .clientesAtendidos(clientesAtendidos)
                .mediaAvaliacao(4.8)
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

                // Últimos 7 dias (de hoje até 6 dias atrás)
                for (int i = 6; i >= 0; i--) {
                    LocalDate data = LocalDate.now().minusDays(i);
                    String nomeDia = data.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    labels.add(nomeDia);

                    LocalDateTime inicio = data.atStartOfDay();
                    LocalDateTime fim = data.plusDays(1).atStartOfDay();
                    Long count = agendamentoRepository.countByBarbeariaIdAndStatusAndDataHoraBetween(
                            barbeariaId, StatusAgendamento.CONCLUIDO, inicio, fim);
                    valores.add(count != null ? count : 0L);

                }
                break;

            case "mes":

                // Últimas 4 semanas
                for (int i = 3; i >= 0; i--) {
                    LocalDate inicioSemana = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                    LocalDate fimSemana = inicioSemana.plusDays(6);

                    String label;
                    if (i == 0) {
                        label = "Esta semana";
                    } else if (i == 1) {
                        label = "Semana passada";
                    } else {
                        label = String.format("Há %d semanas", i);
                    }
                    labels.add(label);

                    LocalDateTime inicio = inicioSemana.atStartOfDay();
                    LocalDateTime fim = fimSemana.plusDays(1).atStartOfDay();
                    Long count = agendamentoRepository.countByBarbeariaIdAndStatusAndDataHoraBetween(
                            barbeariaId, StatusAgendamento.CONCLUIDO, inicio, fim);
                    valores.add(count != null ? count : 0L);

                }
                break;

            case "ano":

                // Últimos 12 meses
                for (int i = 11; i >= 0; i--) {
                    LocalDate data = LocalDate.now().minusMonths(i);
                    String nomeMes = data.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    labels.add(nomeMes);

                    YearMonth yearMonth = YearMonth.from(data);
                    LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
                    LocalDateTime fim = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                    Long count = agendamentoRepository.countByBarbeariaIdAndStatusAndDataHoraBetween(
                            barbeariaId, StatusAgendamento.CONCLUIDO, inicio, fim);
                    valores.add(count != null ? count : 0L);

                }
                break;

            default:

                // Padrão: últimas 4 semanas
                for (int i = 3; i >= 0; i--) {
                    LocalDate inicioSemana = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                    LocalDate fimSemana = inicioSemana.plusDays(6);
                    labels.add(String.format("Semana %d", 4 - i));

                    LocalDateTime inicio = inicioSemana.atStartOfDay();
                    LocalDateTime fim = fimSemana.plusDays(1).atStartOfDay();
                    Long count = agendamentoRepository.countByBarbeariaIdAndStatusAndDataHoraBetween(
                            barbeariaId, StatusAgendamento.CONCLUIDO, inicio, fim);
                    valores.add(count != null ? count : 0L);
                }
                break;
        }



        return GraficoDTO.builder()
                .labels(labels)
                .valores(valores)
                .build();
    }
}