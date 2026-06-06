package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.FuncionarioBarbearia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioBarbeariaRepository extends JpaRepository<FuncionarioBarbearia, String> {

    Optional<FuncionarioBarbearia> findByFuncionarioIdAndBarbeariaIdAndAtivoTrue(
            String funcionarioId, String barbeariaId);

    Optional<FuncionarioBarbearia> findByFuncionarioIdAndBarbeariaId(
            String funcionarioId, String barbeariaId);


    List<FuncionarioBarbearia> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    @Query("SELECT fb FROM FuncionarioBarbearia fb " +
            "WHERE fb.barbearia.id = :barbeariaId " +
            "AND fb.ativo = true " +
            "AND fb.disponivel = true")
    List<FuncionarioBarbearia> findFuncionariosDisponiveisPorBarbearia(@Param("barbeariaId") String barbeariaId);

    boolean existsByFuncionarioIdAndAtivoTrue(String funcionarioId);

    List<FuncionarioBarbearia> findByFuncionarioIdAndAtivoTrue(String funcionarioId);

    boolean existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(String funcionarioId, String barbeariaId);

    boolean existsByFuncionarioIdAndBarbeariaIdAndAtivoTrueAndDisponivelTrue(
            String funcionarioId, String barbeariaId);

    @Query("SELECT fb FROM FuncionarioBarbearia fb " +
            "JOIN fb.funcionario f " +
            "WHERE fb.barbearia.id = :barbeariaId " +
            "AND fb.ativo = true " +
            "AND fb.disponivel = true " +
            "AND f.active = true")
    List<FuncionarioBarbearia> findFuncionariosDisponiveisParaAgendamento(@Param("barbeariaId") String barbeariaId);

}