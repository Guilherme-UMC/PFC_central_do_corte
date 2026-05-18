package com.centraldocorte.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgendamentoResponseDTO {
    private String id;
    private LocalDateTime dataHora;
    private String observacoes;
    private String status;
    private LocalDateTime criadoEm;
    private String clienteNome;
    private String barbeariaNome;
    private String funcionarioNome;
    private List<ServicoDTO> servicos;
    private BigDecimal valorTotal;
    private Integer duracaoTotalMinutos;
}