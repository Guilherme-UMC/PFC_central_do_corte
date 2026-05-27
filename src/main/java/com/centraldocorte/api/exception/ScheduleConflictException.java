package com.centraldocorte.api.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduleConflictException extends BusinessException {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ScheduleConflictException(String funcionarioNome, LocalDateTime dataHora) {
        super(String.format(
                "Conflito de horário: o funcionário '%s' já possui agendamento em %s",
                funcionarioNome,
                dataHora.format(FORMATO_DATA_HORA)
        ));
    }

    public ScheduleConflictException(String mensagem) {
        super(mensagem);
    }
}