package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, String> {
    List<Servico> findByBarbeariaIdAndAtivoTrue(String barbeariaId);
    List<Servico> findByBarbeariaId(String barbeariaId);
}