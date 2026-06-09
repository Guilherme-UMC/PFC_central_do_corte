package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.domain.models.enums.DiaSemana;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.HorarioFuncionamentoRepository;
import com.centraldocorte.api.dto.HorarioFuncionamentoDTO;
import com.centraldocorte.api.dto.HorarioDisponivelDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioFuncionamentoRepository horarioRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final DisponibilidadeService disponibilidadeService;

    public List<HorarioFuncionamento> getHorariosByBarbearia(String barbeariaId) {
        return horarioRepository.findByBarbeariaIdOrderByDia(barbeariaId);
    }

    @Transactional
    public List<HorarioFuncionamento> saveHorarios(String barbeariaId, List<HorarioFuncionamentoDTO> horariosDTO) {
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        List<HorarioFuncionamento> horariosSalvos = new ArrayList<>();

        for (HorarioFuncionamentoDTO dto : horariosDTO) {
            DiaSemana dia = DiaSemana.valueOf(dto.getDia());
            HorarioFuncionamento horario = horarioRepository
                    .findByBarbeariaIdAndDia(barbeariaId, dia)
                    .orElse(new HorarioFuncionamento());

            horario.setBarbearia(barbearia);
            horario.setDia(dia);
            horario.setFechado(dto.getFechado() != null ? dto.getFechado() : false);

            if (!horario.getFechado()) {
                if (dto.getHoraAbertura() == null || dto.getHoraFechamento() == null) {
                    throw new BusinessException("Horário de abertura e fechamento são obrigatórios quando não está fechado");
                }
                horario.setHoraAbertura(LocalTime.parse(dto.getHoraAbertura()));
                horario.setHoraFechamento(LocalTime.parse(dto.getHoraFechamento()));

                if (horario.getHoraAbertura().isAfter(horario.getHoraFechamento())) {
                    throw new BusinessException("Horário de abertura deve ser anterior ao fechamento");
                }
            } else {
                horario.setHoraAbertura(null);
                horario.setHoraFechamento(null);
            }

            horariosSalvos.add(horarioRepository.save(horario));
        }

        return horariosSalvos;
    }

    private DiaSemana converterParaDiaSemana(LocalDateTime dataHora) {
        String diaIngles = dataHora.getDayOfWeek().name();
        switch (diaIngles) {
            case "MONDAY": return DiaSemana.SEGUNDA;
            case "TUESDAY": return DiaSemana.TERCA;
            case "WEDNESDAY": return DiaSemana.QUARTA;
            case "THURSDAY": return DiaSemana.QUINTA;
            case "FRIDAY": return DiaSemana.SEXTA;
            case "SATURDAY": return DiaSemana.SABADO;
            case "SUNDAY": return DiaSemana.DOMINGO;
            default: throw new IllegalArgumentException("Dia inválido: " + diaIngles);
        }
    }

    private DiaSemana converterParaDiaSemana(LocalDate data) {
        String diaIngles = data.getDayOfWeek().name();
        switch (diaIngles) {
            case "MONDAY": return DiaSemana.SEGUNDA;
            case "TUESDAY": return DiaSemana.TERCA;
            case "WEDNESDAY": return DiaSemana.QUARTA;
            case "THURSDAY": return DiaSemana.QUINTA;
            case "FRIDAY": return DiaSemana.SEXTA;
            case "SATURDAY": return DiaSemana.SABADO;
            case "SUNDAY": return DiaSemana.DOMINGO;
            default: throw new IllegalArgumentException("Dia inválido: " + diaIngles);
        }
    }

    public boolean isBarbeariaAberta(String barbeariaId, LocalDateTime dataHora) {
        if (dataHora == null) return false;

        DiaSemana dia = converterParaDiaSemana(dataHora);
        HorarioFuncionamento horario = horarioRepository
                .findByBarbeariaIdAndDia(barbeariaId, dia)
                .orElse(null);

        if (horario == null || horario.getFechado()) return false;

        LocalTime hora = dataHora.toLocalTime();
        return !hora.isBefore(horario.getHoraAbertura()) &&
                !hora.isAfter(horario.getHoraFechamento());
    }

    public List<HorarioDisponivelDTO> getHorariosDisponiveisParaAgendamento(
            String barbeariaId,
            LocalDate data,
            Integer duracaoServicoMinutos) {

        List<HorarioDisponivelDTO> horariosDisponiveis = new ArrayList<>();

        DiaSemana dia = converterParaDiaSemana(data);

        HorarioFuncionamento horarioFuncionamento = horarioRepository
                .findByBarbeariaIdAndDia(barbeariaId, dia)
                .orElse(null);

        if (horarioFuncionamento == null || horarioFuncionamento.getFechado()) {
            return horariosDisponiveis;
        }

        LocalTime inicio = horarioFuncionamento.getHoraAbertura();
        LocalTime fim = horarioFuncionamento.getHoraFechamento();

        if (inicio == null || fim == null) {
            return horariosDisponiveis;
        }

        LocalDateTime dataHoraInicio = LocalDateTime.of(data, inicio);
        LocalDateTime dataHoraFim = LocalDateTime.of(data, fim);

        LocalDateTime current = dataHoraInicio;
        while (current.isBefore(dataHoraFim)) {
            boolean disponivel = disponibilidadeService.isHorarioDisponivel(barbeariaId, current);
            horariosDisponiveis.add(new HorarioDisponivelDTO(current, disponivel));
            current = current.plusMinutes(30);
        }

        return horariosDisponiveis;
    }
}