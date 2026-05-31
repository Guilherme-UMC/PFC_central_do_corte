package com.centraldocorte.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequestDTO {

    @NotNull(message = "ID da barbearia é obrigatório")
    private String barbeariaId;

    @NotNull(message = "ID do serviço é obrigatório")
    private String servicoId;

    // ID do funcionário (opcional)
    private String funcionarioId;

    @NotNull(message = "Data e hora são obrigatórias")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    private String observacao;
}