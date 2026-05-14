package com.example.crud.domain.repositories;

import com.example.crud.domain.models.FuncionarioBarbearia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioBarbeariaRepository extends JpaRepository<FuncionarioBarbearia, String> {

    List<FuncionarioBarbearia> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    List<FuncionarioBarbearia> findByFuncionarioIdAndAtivoTrue(String funcionarioId);

    Optional<FuncionarioBarbearia> findByFuncionarioIdAndBarbeariaId(String funcionarioId, String barbeariaId);

    // ⬇️ ADICIONE ESTE MÉTODO ⬇️
    boolean existsByFuncionarioIdAndAtivoTrue(String funcionarioId);

    boolean existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(String funcionarioId, String barbeariaId);
}