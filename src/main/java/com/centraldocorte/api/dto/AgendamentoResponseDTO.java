package com.centraldocorte.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoResponseDTO {
    private String id;
    private String barbeariaNome;
    private String clienteNome;
    private String servicoNome;
    private Double servicoPreco;
    private Integer servicoDuracao;

    private String funcionarioId;
    private String funcionarioNome;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dataHora;

    private String status;
    private String observacao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime criadoEm;
}