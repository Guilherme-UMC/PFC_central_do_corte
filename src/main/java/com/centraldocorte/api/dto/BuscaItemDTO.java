package com.centraldocorte.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuscaItemDTO {
    private String id;
    private String tipo;
    private String nome;
    private String descricao;
    private String imagemUrl;
    private String link;
    private String subtitulo;
    private Double avaliacao;
}
