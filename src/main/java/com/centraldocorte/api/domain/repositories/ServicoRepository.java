package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.models.enums.CategoriaServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, String> {

    List<Servico> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    List<Servico> findByBarbeariaId(String barbeariaId);

    List<Servico> findByBarbeariaIdAndAtivoTrueAndNomeContainingIgnoreCase(String barbeariaId, String nome);

    List<Servico> findByBarbeariaIdAndAtivoTrueAndPrecoBetween(String barbeariaId, Double precoMin, Double precoMax);

    List<Servico> findByAtivoTrueAndNomeContainingIgnoreCase(String nome);

    List<Servico> findByAtivoTrueAndDescricaoContainingIgnoreCase(String descricao);

    List<Servico> findByBarbeariaIdAndAtivoTrueAndCategoria(String barbeariaId, CategoriaServico categoria);

    List<Servico> findByAtivoTrueAndCategoria(CategoriaServico categoria);

    @Query("SELECT DISTINCT s.barbearia FROM Servico s WHERE s.ativo = true AND s.categoria = :categoria")
    List<Barbearia> findBarbeariasByCategoria(@Param("categoria") CategoriaServico categoria);
}