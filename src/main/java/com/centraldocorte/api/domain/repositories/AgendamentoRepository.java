package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByBarbeariaIdOrderByDataHoraDesc(String barbeariaId);
    List<Agendamento> findByClienteIdOrderByDataHoraDesc(String clienteId);
    boolean existsByBarbeariaIdAndDataHoraAndStatusNot(String barbeariaId, LocalDateTime dataHora, StatusAgendamento status);
    List<Agendamento> findByBarbeariaIdAndDataHoraBetween(String barbeariaId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND DATE(a.dataHora) = CURRENT_DATE ORDER BY a.dataHora")
    List<Agendamento> findAgendamentosDoDia(@Param("barbeariaId") String barbeariaId);

    // ✅ CORRIGIDO: usar <> em vez de !=
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosConfirmadosNoHorario(@Param("barbeariaId") String barbeariaId, @Param("dataHora") LocalDateTime dataHora);

    // ===== MÉTODOS PARA FUNCIONÁRIO =====
    List<Agendamento> findByFuncionarioIdAndDataHoraBetween(String funcionarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND DATE(a.dataHora) = :data ORDER BY a.dataHora")
    List<Agendamento> findByFuncionarioIdAndData(@Param("funcionarioId") String funcionarioId, @Param("data") LocalDateTime data);

    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND a.barbearia.id = :barbeariaId AND a.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findByFuncionarioIdAndBarbeariaIdAndDataHoraBetween(
            @Param("funcionarioId") String funcionarioId,
            @Param("barbeariaId") String barbeariaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosPorFuncionarioNoHorario(@Param("funcionarioId") String funcionarioId, @Param("dataHora") LocalDateTime dataHora);

    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND DATE(a.dataHora) = CURRENT_DATE ORDER BY a.dataHora")
    List<Agendamento> findAgendaHojeDoFuncionario(@Param("funcionarioId") String funcionarioId);
}