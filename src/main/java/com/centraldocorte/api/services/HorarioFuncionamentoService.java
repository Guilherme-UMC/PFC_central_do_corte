package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.HorarioFuncionamentoRepository;
import com.centraldocorte.api.dto.HorarioFuncionamentoRequestDTO;
import com.centraldocorte.api.dto.HorarioFuncionamentoResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioFuncionamentoService {
    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public HorarioFuncionamentoResponseDTO criarHorario(String barbeariaId, HorarioFuncionamentoRequestDTO dto) {
        Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);
        validarIntervaloDeHorario(dto);

        HorarioFuncionamento horario = montarHorarioAPartirDoRequest(dto, barbearia);
        HorarioFuncionamento horarioSalvo = horarioFuncionamentoRepository.save(horario);
        return converterParaResponseDTO(horarioSalvo);
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoResponseDTO> listarHorariosBarbearia(String barbeariaId) {
        buscarBarbeariaPorId(barbeariaId);
        return horarioFuncionamentoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoResponseDTO> buscarHorariosPorDiaDaSemana(String barbeariaId, DayOfWeek diaSemana) {
        buscarBarbeariaPorId(barbeariaId);
        return horarioFuncionamentoRepository.findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana)
                .stream()
                .filter(HorarioFuncionamento::getAtivo)
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public HorarioFuncionamentoResponseDTO atualizarHorario(String horarioId, HorarioFuncionamentoRequestDTO dto) {
        HorarioFuncionamento horario = buscarHorarioPorId(horarioId);
        validarIntervaloDeHorario(dto);

        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFim(dto.getHoraFim());

        HorarioFuncionamento horarioSalvo = horarioFuncionamentoRepository.save(horario);
        return converterParaResponseDTO(horarioSalvo);
    }

    @Transactional
    public void desativarHorario(String horarioId) {
        HorarioFuncionamento horario = buscarHorarioPorId(horarioId);
        horario.setAtivo(false);
        horarioFuncionamentoRepository.save(horario);
    }

    @Transactional
    public void ativarHorario(String horarioId) {
        HorarioFuncionamento horario = buscarHorarioPorId(horarioId);
        horario.setAtivo(true);
        horarioFuncionamentoRepository.save(horario);
    }

    @Transactional
    public void removerHorarioPermanentemente(String horarioId) {
        HorarioFuncionamento horario = buscarHorarioPorId(horarioId);
        horarioFuncionamentoRepository.delete(horario);
    }

    @Transactional(readOnly = true)
    public boolean isHorarioValido(String barbeariaId, DayOfWeek diaSemana, LocalTime hora) {
        return horarioFuncionamentoRepository.findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana)
                .stream()
                .filter(HorarioFuncionamento::getAtivo)
                .anyMatch(horario -> horarioEstaNoIntervalo(hora, horario));
    }

    private Barbearia buscarBarbeariaPorId(String barbeariaId) {
        return barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));
    }

    private HorarioFuncionamento buscarHorarioPorId(String horarioId) {
        return horarioFuncionamentoRepository.findById(horarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Horário não encontrado: " + horarioId));
    }

    private void validarIntervaloDeHorario(HorarioFuncionamentoRequestDTO dto) {
        boolean horarioInicioInvalido = dto.getHoraInicio().isAfter(dto.getHoraFim())
                || dto.getHoraInicio().equals(dto.getHoraFim());

        if (horarioInicioInvalido) {
            throw new BusinessException("Hora de início deve ser anterior à hora de fim");
        }
    }

    private boolean horarioEstaNoIntervalo(LocalTime hora, HorarioFuncionamento horario) {
        return !hora.isBefore(horario.getHoraInicio()) && !hora.isAfter(horario.getHoraFim());
    }

    private HorarioFuncionamento montarHorarioAPartirDoRequest(HorarioFuncionamentoRequestDTO dto, Barbearia barbearia) {
        HorarioFuncionamento horario = new HorarioFuncionamento();
        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFim(dto.getHoraFim());
        horario.setAtivo(true);
        horario.setBarbearia(barbearia);
        return horario;
    }

    private HorarioFuncionamentoResponseDTO converterParaResponseDTO(HorarioFuncionamento horario) {
        return HorarioFuncionamentoResponseDTO.builder()
                .id(horario.getId())
                .diaSemana(horario.getDiaSemana())
                .horaInicio(horario.getHoraInicio())
                .horaFim(horario.getHoraFim())
                .ativo(horario.getAtivo())
                .barbeariaId(horario.getBarbearia().getId())
                .barbeariaNome(horario.getBarbearia().getNome())
                .build();
    }
}