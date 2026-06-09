package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
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

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosConfirmadosNoHorario(@Param("barbeariaId") String barbeariaId, @Param("dataHora") LocalDateTime dataHora);

    List<Agendamento> findByFuncionarioIdAndDataHoraBetween(String funcionarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosPorFuncionarioNoHorario(@Param("funcionarioId") String funcionarioId, @Param("dataHora") LocalDateTime dataHora);

    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId " +
            "AND a.dataHora > :agora " +
            "AND a.status <> 'CANCELADO_PELO_CLIENTE' " +
            "AND a.status <> 'CANCELADO_PELA_BARBEARIA' " +
            "AND a.status <> 'CONCLUIDO' " +
            "ORDER BY a.dataHora ASC")
    List<Agendamento> findFutureAgendamentosByFuncionarioId(@Param("funcionarioId") String funcionarioId, @Param("agora") LocalDateTime agora);

    @Modifying
    @Query("UPDATE Agendamento a SET a.funcionario.id = :novoFuncionarioId, " +
            "a.observacao = CONCAT(a.observacao, :observacaoTransferencia) " +
            "WHERE a.id = :agendamentoId")
    void transferirAgendamentoParaOutroFuncionario(@Param("agendamentoId") Long agendamentoId,
                                                   @Param("novoFuncionarioId") String novoFuncionarioId,
                                                   @Param("observacaoTransferencia") String observacaoTransferencia);

    List<Agendamento> findByFuncionarioIdOrderByDataHoraDesc(String funcionarioId);

    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND DATE(a.dataHora) = CURRENT_DATE ORDER BY a.dataHora")
    List<Agendamento> findAgendamentosDoDiaByFuncionarioId(@Param("funcionarioId") String funcionarioId);

    @Query("SELECT a FROM Agendamento a WHERE a.cliente.id = :clienteId " +
            "AND a.dataHora > :agora " +
            "AND a.status NOT IN :statusesIgnorados")
    List<Agendamento> findByClienteIdAndDataHoraAfterAndStatusNotIn(
            @Param("clienteId") String clienteId,
            @Param("agora") LocalDateTime agora,
            @Param("statusesIgnorados") List<StatusAgendamento> statusesIgnorados);

    @Query("SELECT a FROM Agendamento a WHERE a.barbearia.id = :barbeariaId " +
            "AND a.dataHora > :agora " +
            "AND a.status NOT IN :statusesIgnorados")
    List<Agendamento> findByBarbeariaIdAndDataHoraAfterAndStatusNotIn(
            @Param("barbeariaId") String barbeariaId,
            @Param("agora") LocalDateTime agora,
            @Param("statusesIgnorados") List<StatusAgendamento> statusesIgnorados);


    Long countByBarbeariaId(String barbeariaId);

    Long countByBarbeariaIdAndDataHoraBetween(String barbeariaId, LocalDateTime inicio, LocalDateTime fim);

    Long countByBarbeariaIdAndStatus(String barbeariaId, StatusAgendamento status);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.status = :status AND a.dataHora BETWEEN :inicio AND :fim")
    Long countByBarbeariaIdAndStatusAndDataHoraBetween(
            @Param("barbeariaId") String barbeariaId,
            @Param("status") StatusAgendamento status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.status IN :status")
    Long countByBarbeariaIdAndStatusIn(
            @Param("barbeariaId") String barbeariaId,
            @Param("status") List<StatusAgendamento> status);

    @Query("SELECT COUNT(DISTINCT a.cliente.id) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.status = :status")
    Long countDistinctClientesByBarbeariaIdAndStatus(
            @Param("barbeariaId") String barbeariaId,
            @Param("status") StatusAgendamento status);

    boolean existsByFuncionarioIdAndDataHoraBetween(String funcionarioId, LocalDateTime inicio, LocalDateTime fim);
}