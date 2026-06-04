package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Buscar por barbearia
    List<Agendamento> findByBarbeariaIdOrderByDataHoraDesc(String barbeariaId);

    // Buscar por cliente
    List<Agendamento> findByClienteIdOrderByDataHoraDesc(String clienteId);

    // Verificar existência por horário
    boolean existsByBarbeariaIdAndDataHoraAndStatusNot(String barbeariaId, LocalDateTime dataHora, StatusAgendamento status);

    // Buscar entre datas
    List<Agendamento> findByBarbeariaIdAndDataHoraBetween(String barbeariaId, LocalDateTime inicio, LocalDateTime fim);

    // Agendamentos do dia
    @Query("SELECT a FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND DATE(a.dataHora) = CURRENT_DATE ORDER BY a.dataHora")
    List<Agendamento> findAgendamentosDoDia(@Param("barbeariaId") String barbeariaId);

    // Contar agendamentos confirmados no horário (barbearia)
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosConfirmadosNoHorario(@Param("barbeariaId") String barbeariaId, @Param("dataHora") LocalDateTime dataHora);

    // Buscar agendamentos de um funcionário em um período
    List<Agendamento> findByFuncionarioIdAndDataHoraBetween(String funcionarioId, LocalDateTime inicio, LocalDateTime fim);

    // Contar agendamentos de um funcionário em um horário específico
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.funcionario.id = :funcionarioId AND a.dataHora = :dataHora AND a.status <> 'CANCELADO_PELO_CLIENTE' AND a.status <> 'CANCELADO_PELA_BARBEARIA'")
    long countAgendamentosPorFuncionarioNoHorario(@Param("funcionarioId") String funcionarioId, @Param("dataHora") LocalDateTime dataHora);

    //Busca agendamentos futuros de um funcionário (não cancelados e não concluídos)
    @Query("SELECT a FROM Agendamento a WHERE a.funcionario.id = :funcionarioId " +
            "AND a.dataHora > :agora " +
            "AND a.status <> 'CANCELADO_PELO_CLIENTE' " +
            "AND a.status <> 'CANCELADO_PELA_BARBEARIA' " +
            "AND a.status <> 'CONCLUIDO' " +
            "ORDER BY a.dataHora ASC")
    List<Agendamento> findFutureAgendamentosByFuncionarioId(@Param("funcionarioId") String funcionarioId,
                                                            @Param("agora") LocalDateTime agora);

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
}