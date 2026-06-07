package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.FaturamentoMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FaturamentoMensalRepository extends JpaRepository<FaturamentoMensal, String> {

    Optional<FaturamentoMensal> findByBarbeariaIdAndAnoAndMes(String barbeariaId, Integer ano, Integer mes);

    @Query("SELECT f.valorTotal FROM FaturamentoMensal f WHERE f.barbeariaId = :barbeariaId AND f.ano = :ano AND f.mes = :mes")
    BigDecimal findValorTotalByBarbeariaAndMes(@Param("barbeariaId") String barbeariaId, @Param("ano") Integer ano, @Param("mes") Integer mes);

    @Query("SELECT COALESCE(SUM(f.valorTotal), 0) FROM FaturamentoMensal f WHERE f.barbeariaId = :barbeariaId AND f.ano = :ano")
    BigDecimal findTotalFaturamentoAno(@Param("barbeariaId") String barbeariaId, @Param("ano") Integer ano);
}