package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {

    private final AgendamentoRepository agendamentoRepository;

    /**
     * Verifica se um horário está disponível para agendamento (geral da barbearia)
     */
    public boolean isHorarioDisponivel(String barbeariaId, LocalDateTime dataHora) {
        long count = agendamentoRepository.countAgendamentosConfirmadosNoHorario(barbeariaId, dataHora);
        return count == 0;
    }

    /**
     * Verifica se um funcionário está disponível em um determinado horário
     */
    public boolean isHorarioDisponivelParaFuncionario(String funcionarioId, LocalDateTime dataHora) {
        long count = agendamentoRepository.countAgendamentosPorFuncionarioNoHorario(funcionarioId, dataHora);
        return count == 0;
    }
}