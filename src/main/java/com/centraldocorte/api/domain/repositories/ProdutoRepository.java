package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, String> {

    Page<Produto> findByBarbeariaIdAndAtivoTrue(String barbeariaId, Pageable pageable);

    List<Produto> findByBarbeariaIdAndAtivoTrue(String barbeariaId);

    List<Produto> findByBarbeariaIdAndAtivoTrueAndCategoria(String barbeariaId, String categoria);

    @Query("SELECT DISTINCT p.categoria FROM Produto p WHERE p.barbearia.id = :barbeariaId AND p.ativo = true")
    List<String> findCategoriasByBarbeariaId(@Param("barbeariaId") String barbeariaId);
}