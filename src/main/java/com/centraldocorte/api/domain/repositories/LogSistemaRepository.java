package com.centraldocorte.api.domain.repositories;

import com.centraldocorte.api.domain.models.LogSistema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {

    Page<LogSistema> findAllByOrderByDataHoraDesc(Pageable pageable);

    Page<LogSistema> findByTipoContainingIgnoreCaseOrderByDataHoraDesc(String tipo, Pageable pageable);

    Page<LogSistema> findByAcaoContainingIgnoreCaseOrderByDataHoraDesc(String acao, Pageable pageable);

    Page<LogSistema> findByUsuarioIdOrderByDataHoraDesc(String usuarioId, Pageable pageable);

    Page<LogSistema> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    Page<LogSistema> findByTipoContainingIgnoreCaseAndAcaoContainingIgnoreCaseOrderByDataHoraDesc(
            String tipo, String acao, Pageable pageable);

    @Query("SELECT DISTINCT l.tipo FROM LogSistema l ORDER BY l.tipo")
    List<String> findDistinctTipos();

    @Query("SELECT DISTINCT l.acao FROM LogSistema l ORDER BY l.acao")
    List<String> findDistinctAcoes();

    @Query("SELECT l.tipo, COUNT(l) FROM LogSistema l GROUP BY l.tipo")
    List<Object[]> countByTipo();
}