package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.domain.models.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, Long> {

    List<HorarioFuncionamento> findByBarbeariaIdOrderByDia(String barbeariaId);

    Optional<HorarioFuncionamento> findByBarbeariaIdAndDia(String barbeariaId, DiaSemana dia);

    void deleteByBarbeariaId(String barbeariaId);
}