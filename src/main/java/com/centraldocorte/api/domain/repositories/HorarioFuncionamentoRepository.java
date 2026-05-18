package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, String> {
    List<HorarioFuncionamento> findByBarbeariaIdAndAtivoTrue(String barbeariaId);
    List<HorarioFuncionamento> findByBarbeariaId(String barbeariaId);
    List<HorarioFuncionamento> findByBarbeariaIdAndDiaSemana(String barbeariaId, DayOfWeek diaSemana);
}