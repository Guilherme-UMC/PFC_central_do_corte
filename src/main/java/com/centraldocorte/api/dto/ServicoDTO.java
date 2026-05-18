package com.centraldocorte.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServicoDTO {
    private String id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer duracaoMinutos;
    private Boolean ativo;
}