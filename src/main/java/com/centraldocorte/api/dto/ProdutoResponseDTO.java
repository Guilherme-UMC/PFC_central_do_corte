package com.centraldocorte.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponseDTO {

    private String id;
    private String barbeariaId;
    private String barbeariaNome;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String imagemUrl;
    private String categoria;
    private String marca;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}