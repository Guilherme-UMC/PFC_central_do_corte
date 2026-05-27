package com.centraldocorte.api.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioFuncionamentoDTO {

    private Long id;

    @NotNull(message = "Dia é obrigatório")
    private String dia;

    private String horaAbertura;

    private String horaFechamento;

    private Boolean fechado = false;
}