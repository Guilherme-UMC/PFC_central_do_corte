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
public class LogSistemaFiltroDTO {
    private String tipo;
    private String acao;
    private String usuarioId;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}