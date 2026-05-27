package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.FuncionarioBarbearia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioBarbeariaRepository extends JpaRepository<FuncionarioBarbearia, Long> {

    // Buscar vínculos ativos de uma barbearia
    List<FuncionarioBarbearia> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    // Buscar vínculos ativos de um funcionário
    List<FuncionarioBarbearia> findByFuncionarioIdAndAtivoTrue(String funcionarioId);

    // Buscar vínculo específico por funcionário e barbearia
    Optional<FuncionarioBarbearia> findByFuncionarioIdAndBarbeariaId(String funcionarioId, String barbeariaId);

    // Verificar se funcionário já está vinculado ativo a uma barbearia
    boolean existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(String funcionarioId, String barbeariaId);

    // Verificar se funcionário tem vínculo ativo com alguma barbearia
    boolean existsByFuncionarioIdAndAtivoTrue(String funcionarioId);

    // Buscar todas as barbearias de um funcionário (vínculos ativos)
    List<FuncionarioBarbearia> findByFuncionarioId(String funcionarioId);

    // Desativar todos os vínculos de um funcionário (útil para demissão)
    void deleteByFuncionarioId(String funcionarioId);
}