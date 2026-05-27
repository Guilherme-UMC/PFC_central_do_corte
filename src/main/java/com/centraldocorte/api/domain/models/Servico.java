package com.centraldocorte.api.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "servicos")

    public class Servico {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        @Column(nullable = false)
        private String nome;

        private String descricao;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal preco;

        @Column(nullable = false)
        private Integer duracaoMinutos;

        @Column(nullable = false)
        private Boolean ativo = true;

        @ManyToOne
        @JoinColumn(name = "barbearia_id", nullable = false)
        private Barbearia barbearia;
    }

