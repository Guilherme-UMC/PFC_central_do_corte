package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {

    private final AgendamentoRepository agendamentoRepository;

    public boolean isHorarioDisponivel(String barbeariaId, LocalDateTime dataHora) {
        long count = agendamentoRepository.countAgendamentosConfirmadosNoHorario(barbeariaId, dataHora);
        return count == 0;
    }

    public boolean isHorarioDisponivelParaFuncionario(String funcionarioId, LocalDateTime dataHora) {
        long count = agendamentoRepository.countAgendamentosPorFuncionarioNoHorario(funcionarioId, dataHora);
        return count == 0;
    }
}