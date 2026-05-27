package com.centraldocorte.api.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDisponivelDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime horario;

    private Boolean disponivel;
    private String horaFormatada;

    public HorarioDisponivelDTO(LocalDateTime horario, Boolean disponivel) {
        this.horario = horario;
        this.disponivel = disponivel;
        this.horaFormatada = String.format("%02d:%02d", horario.getHour(), horario.getMinute());
    }
}