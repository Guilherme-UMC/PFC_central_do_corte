package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.dto.ServicoDTO;
import com.centraldocorte.api.exception.ScheduleConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final ServicoRepository servicoRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final HorarioFuncionamentoService horarioFuncionamentoService;

    @Transactional
    public Agendamento criarAgendamento(String clienteId, AgendamentoRequestDTO dto) {
        Usuario cliente = buscarClientePorId(clienteId);
        Barbearia barbearia = buscarBarbeariaPorId(dto.getBarbeariaId());
        Usuario funcionario = buscarFuncionarioPorId(dto.getFuncionarioId());

        validarFuncionarioPertenceABarbearia(funcionario.getId(), barbearia.getId());
        validarHorarioDentroDoExpediente(barbearia.getId(), dto.getDataHora());

        List<Servico> servicos = buscarServicos(dto.getServicosIds());
        validarDisponibilidadeDoFuncionario(funcionario, dto.getDataHora(), servicos);

        Agendamento agendamento = montarAgendamento(cliente, barbearia, funcionario, dto, servicos);
        return agendamentoRepository.save(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosCliente(String clienteId) {
        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarAgendamentosBarbearia(String barbeariaId) {
        return agendamentoRepository.findByBarbeariaIdOrderByDataHoraAsc(barbeariaId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public Agendamento atualizarStatusAgendamento(String agendamentoId, String novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado: " + agendamentoId));

        try {
            agendamento.setStatus(StatusAgendamento.valueOf(novoStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + novoStatus);
        }

        return agendamentoRepository.save(agendamento);
    }

    //Metodos de busca
    private Usuario buscarClientePorId(String clienteId) {
        return usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));
    }

    private Barbearia buscarBarbeariaPorId(String barbeariaId) {
        return barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));
    }

    private Usuario buscarFuncionarioPorId(String funcionarioId) {
        return usuarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + funcionarioId));
    }

    private List<Servico> buscarServicos(List<String> servicosIds) {
        List<Servico> servicos = servicoRepository.findAllById(servicosIds);
        if (servicos.isEmpty()) {
            throw new BusinessException("Nenhum serviço válido encontrado");
        }
        return servicos;
    }

    // Metodos de validação
    private void validarFuncionarioPertenceABarbearia(String funcionarioId, String barbeariaId) {
        boolean pertence = funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId);
        if (!pertence) {
            throw new BusinessException("Funcionário não pertence a esta barbearia");
        }
    }

    private void validarHorarioDentroDoExpediente(String barbeariaId, LocalDateTime dataHora) {
        DayOfWeek diaSemana = dataHora.getDayOfWeek();
        LocalTime hora = dataHora.toLocalTime();
        boolean horarioValido = horarioFuncionamentoService.isHorarioValido(barbeariaId, diaSemana, hora);
        if (!horarioValido) {
            throw new BusinessException("Horário fora do expediente da barbearia");
        }
    }

    private void validarDisponibilidadeDoFuncionario(Usuario funcionario, LocalDateTime inicio, List<Servico> servicos) {
        int duracaoTotalMinutos = calcularDuracaoTotal(servicos);
        LocalDateTime horarioFim = inicio.plusMinutes(duracaoTotalMinutos);

        List<Agendamento> agendamentosConflitantes = agendamentoRepository
                .findByFuncionarioIdAndDataHoraBetween(funcionario.getId(), inicio, horarioFim);

        if (!agendamentosConflitantes.isEmpty()) {
            throw new ScheduleConflictException(funcionario.getName(), inicio);
        }
    }

    //Metodos de montagem

    private Agendamento montarAgendamento(
            Usuario cliente, Barbearia barbearia, Usuario funcionario,
            AgendamentoRequestDTO dto, List<Servico> servicos) {

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setBarbearia(barbearia);
        agendamento.setFuncionario(funcionario);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setObservacoes(dto.getObservacoes());
        agendamento.setServicos(servicos);
        return agendamento;
    }

    private AgendamentoResponseDTO converterParaDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataHora(agendamento.getDataHora());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setStatus(agendamento.getStatus().name());
        dto.setCriadoEm(agendamento.getCriadoEm());
        dto.setClienteNome(agendamento.getCliente().getName());
        dto.setBarbeariaNome(agendamento.getBarbearia().getNome());
        dto.setFuncionarioNome(agendamento.getFuncionario().getName());
        dto.setServicos(agendamento.getServicos().stream().map(this::converterServicoParaDTO).toList());
        dto.setValorTotal(calcularValorTotal(agendamento.getServicos()));
        dto.setDuracaoTotalMinutos(calcularDuracaoTotal(agendamento.getServicos()));
        return dto;
    }

    private ServicoDTO converterServicoParaDTO(Servico servico) {
        ServicoDTO dto = new ServicoDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());
        return dto;
    }

    private BigDecimal calcularValorTotal(List<Servico> servicos) {
        return servicos.stream()
                .map(Servico::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calcularDuracaoTotal(List<Servico> servicos) {
        return servicos.stream().mapToInt(Servico::getDuracaoMinutos).sum();
    }
}