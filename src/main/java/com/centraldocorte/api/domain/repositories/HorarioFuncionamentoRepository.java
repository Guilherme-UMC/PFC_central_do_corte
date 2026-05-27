package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.domain.models.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, Long> {

    // CORRIGIDO: Long → String
    List<HorarioFuncionamento> findByBarbeariaIdOrderByDia(String barbeariaId);

    // CORRIGIDO: Long → String
    Optional<HorarioFuncionamento> findByBarbeariaIdAndDia(String barbeariaId, DiaSemana dia);

    // CORRIGIDO: Long → String
    void deleteByBarbeariaId(String barbeariaId);
}