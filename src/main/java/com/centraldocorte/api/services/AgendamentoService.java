package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ServicoRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.repositories.FuncionarioBarbeariaRepository;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import com.centraldocorte.api.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final HorarioService horarioService;
    private final DisponibilidadeService disponibilidadeService;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }

    private AgendamentoResponseDTO convertToResponseDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setBarbeariaNome(agendamento.getBarbearia().getNome());
        dto.setClienteNome(agendamento.getCliente().getName());
        dto.setServicoNome(agendamento.getServico().getNome());

        if (agendamento.getFuncionario() != null) {
            dto.setFuncionarioNome(agendamento.getFuncionario().getName());
            dto.setFuncionarioId(agendamento.getFuncionario().getId());
        }

        BigDecimal preco = agendamento.getServico().getPreco();
        dto.setServicoPreco(preco != null ? preco.doubleValue() : 0.0);

        dto.setServicoDuracao(agendamento.getServico().getDuracaoMinutos() != null ?
                agendamento.getServico().getDuracaoMinutos() : 30);

        dto.setDataHora(agendamento.getDataHora());
        dto.setStatus(agendamento.getStatus().getDescricao());
        dto.setObservacao(agendamento.getObservacao());
        dto.setCriadoEm(agendamento.getCriadoEm());
        return dto;
    }

    @Transactional
    public AgendamentoResponseDTO criarAgendamento(AgendamentoRequestDTO request) {

        Usuario cliente = getUsuarioAutenticado();
        if (cliente.getRole() != UsuarioRole.ROLE_CLIENTE) {
            throw new BusinessException("Apenas clientes podem realizar agendamentos");
        }

        Barbearia barbearia = barbeariaRepository.findById(request.getBarbeariaId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        if (!barbearia.getAtivo()) {
            throw new BusinessException("Esta barbearia está inativa no momento");
        }

        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        if (!servico.getBarbearia().getId().equals(barbearia.getId())) {
            throw new BusinessException("Este serviço não pertence à barbearia selecionada");
        }

        Usuario funcionario = null;
        if (request.getFuncionarioId() != null && !request.getFuncionarioId().isEmpty()) {
            funcionario = usuarioRepository.findById(request.getFuncionarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado"));

            boolean pertenceABarbearia = funcionarioBarbeariaRepository
                    .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionario.getId(), barbearia.getId());

            if (!pertenceABarbearia) {
                throw new BusinessException("Este funcionário não pertence à barbearia selecionada");
            }

            if (funcionario.getRole() != UsuarioRole.ROLE_FUNCIONARIO) {
                throw new BusinessException("O usuário selecionado não é um funcionário");
            }
        }

        if (request.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível agendar para data/hora passada");
        }

        if (!horarioService.isBarbeariaAberta(barbearia.getId(), request.getDataHora())) {
            throw new BusinessException("A barbearia não está aberta neste horário");
        }

        if (funcionario != null) {
            if (!disponibilidadeService.isHorarioDisponivelParaFuncionario(funcionario.getId(), request.getDataHora())) {
                throw new BusinessException("Este funcionário não está disponível no horário selecionado");
            }
        } else {
            if (!disponibilidadeService.isHorarioDisponivel(barbearia.getId(), request.getDataHora())) {
                throw new BusinessException("Este horário já está ocupado");
            }
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setBarbearia(barbearia);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setFuncionario(funcionario);
        agendamento.setDataHora(request.getDataHora());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setObservacao(request.getObservacao());

        Agendamento saved = agendamentoRepository.save(agendamento);
        return convertToResponseDTO(saved);
    }

    @Transactional
    public AgendamentoResponseDTO cancelarAgendamento(Long agendamentoId, String motivo) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        Usuario usuario = getUsuarioAutenticado();
        boolean isCliente = agendamento.getCliente().getId().equals(usuario.getId());
        boolean isDonoBarbearia = agendamento.getBarbearia().getOwner().getId().equals(usuario.getId());

        if (!isCliente && !isDonoBarbearia) {
            throw new UnauthorizedException("Você não tem permissão para cancelar este agendamento");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Não é possível cancelar um agendamento já concluído");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limiteCancelamento = agendamento.getDataHora().minusHours(24);

        if (agendamento.getDataHora().isBefore(agora)) {
            throw new BusinessException("Não é possível cancelar um agendamento que já passou");
        }

        if (agora.isAfter(limiteCancelamento)) {
            throw new BusinessException("Cancelamentos devem ser feitos com pelo menos 24 horas de antecedência");
        }

        if (isCliente) {
            agendamento.setStatus(StatusAgendamento.CANCELADO_PELO_CLIENTE);
        } else {
            agendamento.setStatus(StatusAgendamento.CANCELADO_PELA_BARBEARIA);
        }

        agendamento.setObservacao(String.format("Cancelado por %s. Motivo: %s",
                isCliente ? "cliente" : "barbearia", motivo));

        Agendamento updated = agendamentoRepository.save(agendamento);
        return convertToResponseDTO(updated);
    }

    @Transactional
    public AgendamentoResponseDTO confirmarAgendamento(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        Usuario usuario = getUsuarioAutenticado();
        if (!agendamento.getBarbearia().getOwner().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("Apenas o proprietário da barbearia pode confirmar agendamentos");
        }

        if (agendamento.getStatus() != StatusAgendamento.PENDENTE) {
            throw new BusinessException("Apenas agendamentos pendentes podem ser confirmados");
        }

        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return convertToResponseDTO(updated);
    }


    @Transactional
    public AgendamentoResponseDTO concluirAgendamento(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        Usuario usuario = getUsuarioAutenticado();

        // Permite proprietário OU qualquer usuário com role FUNCIONARIO
        boolean isOwner = agendamento.getBarbearia().getOwner().getId().equals(usuario.getId());
        boolean isFuncionario = usuario.getRole() == UsuarioRole.ROLE_FUNCIONARIO;

        if (!isOwner && !isFuncionario) {
            throw new UnauthorizedException("Apenas o proprietário da barbearia ou funcionários podem concluir agendamentos");
        }

        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser concluídos");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return convertToResponseDTO(updated);
    }

    public List<AgendamentoResponseDTO> getAgendamentosDoCliente() {
        Usuario cliente = getUsuarioAutenticado();
        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(cliente.getId())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponseDTO> getAgendamentosDaBarbearia(String barbeariaId) {
        Usuario usuario = getUsuarioAutenticado();

        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        if (!barbearia.getOwner().getId().equals(usuario.getId()) &&
                !usuario.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedException("Você não tem permissão para ver agendamentos desta barbearia");
        }

        return agendamentoRepository.findByBarbeariaIdOrderByDataHoraDesc(barbeariaId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public List<AgendamentoResponseDTO> getAgendamentosDoDia(String barbeariaId) {
        Usuario usuario = getUsuarioAutenticado();

        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        if (!barbearia.getOwner().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("Você não tem permissão para ver agendamentos desta barbearia");
        }

        return agendamentoRepository.findAgendamentosDoDia(barbeariaId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponseDTO> getAgendamentosDoFuncionario() {
        Usuario funcionario = getUsuarioAutenticado();

        if (funcionario.getRole() != UsuarioRole.ROLE_FUNCIONARIO) {
            throw new UnauthorizedException("Apenas funcionários podem acessar esta funcionalidade");
        }

        List<Agendamento> agendamentos = agendamentoRepository
                .findByFuncionarioIdOrderByDataHoraDesc(funcionario.getId());

        return agendamentos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponseDTO> getAgendamentosDoDiaDoFuncionario() {
        Usuario funcionario = getUsuarioAutenticado();

        if (funcionario.getRole() != UsuarioRole.ROLE_FUNCIONARIO) {
            throw new UnauthorizedException("Apenas funcionários podem acessar esta funcionalidade");
        }

        List<Agendamento> agendamentos = agendamentoRepository
                .findAgendamentosDoDiaByFuncionarioId(funcionario.getId());

        return agendamentos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}