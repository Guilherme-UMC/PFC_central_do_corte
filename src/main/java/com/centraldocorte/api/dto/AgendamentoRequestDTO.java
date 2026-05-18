package com.centraldocorte.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgendamentoRequestDTO {
    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "Data e hora devem ser futuras")
    private LocalDateTime dataHora;

    private String observacoes;

    @NotNull(message = "ID da barbearia é obrigatório")
    private String barbeariaId;

    @NotNull(message = "ID do funcionário é obrigatório")
    private String funcionarioId;

    @NotEmpty(message = "Pelo menos um serviço deve ser selecionado")
    private List<String> servicosIds;
}