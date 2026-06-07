package com.centraldocorte.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogSistemaDTO {
    private Long id;
    private String tipo;
    private String acao;
    private String usuarioId;
    private String usuarioEmail;
    private String usuarioNome;
    private String usuarioRole;
    private String entidade;
    private String entidadeId;
    private String descricao;
    private String detalhes;
    private String ipOrigem;
    private LocalDateTime dataHora;
}