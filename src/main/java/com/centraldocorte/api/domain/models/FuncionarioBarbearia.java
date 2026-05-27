package com.centraldocorte.api.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "funcionarios_barbearia")
public class FuncionarioBarbearia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Usuario funcionario;

    @ManyToOne
    @JoinColumn(name = "barbearia_id", nullable = false)
    private Barbearia barbearia;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
