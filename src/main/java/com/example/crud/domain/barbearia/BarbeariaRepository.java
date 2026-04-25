package com.example.crud.domain.barbearia;

import com.example.crud.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarbeariaRepository extends JpaRepository<Barbearia, String> {

    Optional<Barbearia> findByIdAndAtivoTrue(String id);

    Page<Barbearia> findAllByAtivoTrue(Pageable pageable);

    List<Barbearia> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);

    List<Barbearia> findByCidadeAndUfAndAtivoTrue(String cidade, String uf);

    List<Barbearia> findByOwnerAndAtivoTrue(User owner);

    @Query("SELECT b FROM barbearia b WHERE b.owner.id = :ownerId AND b.ativo = true")
    List<Barbearia> findByOwnerIdAndAtivoTrue(@Param("ownerId") String ownerId);
}