package com.example.crud.services;

import com.example.crud.domain.models.Barbearia;
import com.example.crud.domain.models.HorarioFuncionamento;
import com.example.crud.domain.repositories.BarbeariaRepository;
import com.example.crud.domain.repositories.HorarioFuncionamentoRepository;
import com.example.crud.dto.HorarioFuncionamentoRequestDTO;
import com.example.crud.dto.HorarioFuncionamentoResponseDTO;
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
    private final HorarioFuncionamentoRepository horarioRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public HorarioFuncionamentoResponseDTO criarHorario(String barbeariaId, HorarioFuncionamentoRequestDTO dto) {
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        // Validar se horaInicio é anterior a horaFim
        if (dto.getHoraInicio().isAfter(dto.getHoraFim()) || dto.getHoraInicio().equals(dto.getHoraFim())) {
            throw new RuntimeException("Hora de início deve ser anterior à hora de fim");
        }

        HorarioFuncionamento horario = new HorarioFuncionamento();
        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFim(dto.getHoraFim());
        horario.setAtivo(true);
        horario.setBarbearia(barbearia);

        HorarioFuncionamento saved = horarioRepository.save(horario);
        return convertToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoResponseDTO> listarHorarios(String barbeariaId) {
        barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        return horarioRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HorarioFuncionamentoResponseDTO> buscarHorariosPorDia(String barbeariaId, DayOfWeek diaSemana) {
        barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        return horarioRepository.findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana)
                .stream()
                .filter(HorarioFuncionamento::getAtivo)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HorarioFuncionamentoResponseDTO atualizarHorario(String horarioId, HorarioFuncionamentoRequestDTO dto) {
        HorarioFuncionamento horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado: " + horarioId));

        if (dto.getHoraInicio().isAfter(dto.getHoraFim()) || dto.getHoraInicio().equals(dto.getHoraFim())) {
            throw new RuntimeException("Hora de início deve ser anterior à hora de fim");
        }

        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFim(dto.getHoraFim());

        HorarioFuncionamento updated = horarioRepository.save(horario);
        return convertToResponseDTO(updated);
    }

    @Transactional
    public void desativarHorario(String horarioId) {
        HorarioFuncionamento horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado: " + horarioId));

        horario.setAtivo(false);
        horarioRepository.save(horario);
    }

    @Transactional
    public void ativarHorario(String horarioId) {
        HorarioFuncionamento horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado: " + horarioId));

        horario.setAtivo(true);
        horarioRepository.save(horario);
    }

    @Transactional
    public void removerHorario(String horarioId) {
        HorarioFuncionamento horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado: " + horarioId));

        horarioRepository.delete(horario);
    }

    @Transactional(readOnly = true)
    public boolean isHorarioValido(String barbeariaId, DayOfWeek diaSemana, LocalTime hora) {
        List<HorarioFuncionamento> horarios = horarioRepository
                .findByBarbeariaIdAndDiaSemana(barbeariaId, diaSemana);

        return horarios.stream()
                .filter(HorarioFuncionamento::getAtivo)
                .anyMatch(h ->
                        !hora.isBefore(h.getHoraInicio()) &&
                                !hora.isAfter(h.getHoraFim())
                );
    }

    // Método auxiliar para conversão
    private HorarioFuncionamentoResponseDTO convertToResponseDTO(HorarioFuncionamento horario) {
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