package com.example.crud.services;

import com.example.crud.domain.models.Barbearia;
import com.example.crud.domain.repositories.BarbeariaRepository;
import com.example.crud.domain.models.*;
import com.example.crud.domain.repositories.*;
import com.example.crud.domain.models.User;
import com.example.crud.domain.repositories.UserRepository;
import com.example.crud.dto.AgendamentoRequestDTO;
import com.example.crud.dto.AgendamentoResponseDTO;
import com.example.crud.dto.ServicoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final UserRepository userRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final ServicoRepository servicoRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final HorarioFuncionamentoService horarioService;

    @Transactional
    public Agendamento criarAgendamento(String clienteId, AgendamentoRequestDTO dto) {
        // Validar cliente
        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar barbearia
        Barbearia barbearia = barbeariaRepository.findById(dto.getBarbeariaId())
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada"));

        // Validar funcionário
        User funcionario = userRepository.findById(dto.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

        // Validar se funcionário pertence à barbearia
        if (!funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(
                funcionario.getId(), barbearia.getId())) {
            throw new RuntimeException("Funcionário não pertence a esta barbearia");
        }

        // Validar horário de funcionamento
        DayOfWeek diaSemana = dto.getDataHora().getDayOfWeek();
        java.time.LocalTime hora = dto.getDataHora().toLocalTime();
        if (!horarioService.isHorarioValido(barbearia.getId(), diaSemana, hora)) {
            throw new RuntimeException("Horário fora do expediente da barbearia");
        }

        // Buscar serviços
        List<Servico> servicos = servicoRepository.findAllById(dto.getServicosIds());
        if (servicos.isEmpty()) {
            throw new RuntimeException("Nenhum serviço válido encontrado");
        }

        // Calcular duração total
        int duracaoTotal = servicos.stream().mapToInt(Servico::getDuracaoMinutos).sum();
        LocalDateTime horarioFim = dto.getDataHora().plusMinutes(duracaoTotal);

        // Validar disponibilidade do horário
        List<Agendamento> agendamentosConflitantes = agendamentoRepository
                .findByFuncionarioIdAndDataHoraBetween(funcionario.getId(), dto.getDataHora(), horarioFim);

        if (!agendamentosConflitantes.isEmpty()) {
            throw new RuntimeException("Funcionário já possui agendamento neste horário");
        }

        // Criar agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbearia(barbearia);
        agendamento.setFuncionario(funcionario);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setObservacoes(dto.getObservacoes());
        agendamento.setServicos(servicos);

        return agendamentoRepository.save(agendamento);
    }

    public List<AgendamentoResponseDTO> listarAgendamentosCliente(String clienteId) {
        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AgendamentoResponseDTO> listarAgendamentosBarbearia(String barbeariaId) {
        return agendamentoRepository.findByBarbeariaIdOrderByDataHoraAsc(barbeariaId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public Agendamento atualizarStatus(String agendamentoId, String status) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.valueOf(status.toUpperCase()));
        return agendamentoRepository.save(agendamento);
    }

    private AgendamentoResponseDTO convertToDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataHora(agendamento.getDataHora());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setStatus(agendamento.getStatus().name());
        dto.setCriadoEm(agendamento.getCriadoEm());
        dto.setClienteNome(agendamento.getCliente().getName());
        dto.setBarbeariaNome(agendamento.getBarbearia().getNome());
        dto.setFuncionarioNome(agendamento.getFuncionario().getName());

        List<ServicoDTO> servicosDTO = agendamento.getServicos().stream()
                .map(this::convertServicoToDTO).collect(Collectors.toList());
        dto.setServicos(servicosDTO);

        BigDecimal valorTotal = agendamento.getServicos().stream()
                .map(Servico::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setValorTotal(valorTotal);

        int duracaoTotal = agendamento.getServicos().stream()
                .mapToInt(Servico::getDuracaoMinutos).sum();
        dto.setDuracaoTotalMinutos(duracaoTotal);

        return dto;
    }

    private ServicoDTO convertServicoToDTO(Servico servico) {
        ServicoDTO dto = new ServicoDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());
        return dto;
    }
}