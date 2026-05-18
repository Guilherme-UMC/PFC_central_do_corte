package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, String> {
    List<Agendamento> findByClienteIdOrderByDataHoraDesc(String clienteId);
    List<Agendamento> findByBarbeariaIdOrderByDataHoraAsc(String barbeariaId);
    List<Agendamento> findByFuncionarioIdAndDataHoraBetween(String funcionarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a WHERE a.barbearia.id = :barbeariaId AND a.dataHora BETWEEN :inicio AND :fim AND a.status NOT IN ('CANCELADO', 'CONCLUIDO')")
    List<Agendamento> findAgendamentosAtivosNoPeriodo(String barbeariaId, LocalDateTime inicio, LocalDateTime fim);
}