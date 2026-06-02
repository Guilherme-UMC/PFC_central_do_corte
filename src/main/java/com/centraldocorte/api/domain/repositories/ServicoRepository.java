package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, String> {

    // Buscar serviços ativos de uma barbearia
    List<Servico> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    // Buscar todos os serviços de uma barbearia (ativos e inativos)
    List<Servico> findByBarbeariaId(String barbeariaId);

    // Buscar serviços ativos por nome (busca parcial)
    List<Servico> findByBarbeariaIdAndAtivoTrueAndNomeContainingIgnoreCase(String barbeariaId, String nome);

    // Buscar serviços por faixa de preço
    List<Servico> findByBarbeariaIdAndAtivoTrueAndPrecoBetween(String barbeariaId, Double precoMin, Double precoMax);

    // Buscar serviços ativos pelo nome em QUALQUER barbearia (busca global)
    List<Servico> findByAtivoTrueAndNomeContainingIgnoreCase(String nome);

    // Buscar serviços ativos por descrição (busca global)
    List<Servico> findByAtivoTrueAndDescricaoContainingIgnoreCase(String descricao);
}