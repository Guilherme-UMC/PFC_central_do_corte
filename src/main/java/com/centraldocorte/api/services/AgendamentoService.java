package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    private final LogSistemaService logSistemaService;  // ADICIONADO

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
    public AgendamentoResponseDTO criarAgendamento(AgendamentoRequestDTO request, HttpServletRequest httpRequest) {

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

            if (!disponibilidadeService.isHorarioDisponivelParaFuncionario(funcionario.getId(), request.getDataHora())) {
                throw new BusinessException("Este funcionário não está disponível no horário selecionado");
            }

        } else {

            funcionario = buscarFuncionarioDisponivel(
                    barbearia.getId(),
                    request.getDataHora(),
                    servico.getDuracaoMinutos()
            );

            if (funcionario == null) {
                throw new BusinessException("Nenhum funcionário disponível para este horário. Tente outro horário.");
            }

            log.info("Funcionário {} atribuído automaticamente para agendamento do cliente {}",
                    funcionario.getName(), cliente.getEmail());
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

        log.info("Agendamento criado com sucesso! ID: {}, Funcionário: {}, Cliente: {}",
                saved.getId(), funcionario.getName(), cliente.getName());

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("barbeariaId", barbearia.getId());
        detalhes.put("barbeariaNome", barbearia.getNome());
        detalhes.put("servicoId", servico.getId());
        detalhes.put("servicoNome", servico.getNome());
        detalhes.put("funcionarioId", funcionario.getId());
        detalhes.put("funcionarioNome", funcionario.getName());
        detalhes.put("dataHora", request.getDataHora());

        logSistemaService.registrarLog(
                "AGENDAMENTO",
                "CRIADO",
                "Agendamento",
                saved.getId().toString(),
                String.format("Cliente %s criou agendamento para %s", cliente.getName(), barbearia.getNome()),
                detalhes,
                httpRequest
        );

        return convertToResponseDTO(saved);
    }

    private Usuario buscarFuncionarioDisponivel(String barbeariaId, LocalDateTime dataHora, Integer duracaoMinutos) {

        List<FuncionarioBarbearia> funcionariosVinculados = funcionarioBarbeariaRepository
                .findByBarbeariaIdAndAtivoTrue(barbeariaId);

        if (funcionariosVinculados.isEmpty()) {
            log.warn("Nenhum funcionário vinculado à barbearia: {}", barbeariaId);
            return null;
        }

        int duracao = (duracaoMinutos != null && duracaoMinutos > 0) ? duracaoMinutos : 30;
        LocalDateTime fimExpediente = dataHora.plusMinutes(duracao);

        for (FuncionarioBarbearia vinculo : funcionariosVinculados) {
            Usuario funcionario = vinculo.getFuncionario();

            if (!funcionario.isActive()) {
                log.debug("Funcionário {} está inativo no sistema, ignorando", funcionario.getId());
                continue;
            }

            boolean temConflito = agendamentoRepository.existsByFuncionarioIdAndDataHoraBetween(funcionario.getId(), dataHora, fimExpediente);

            if (!temConflito) {
                log.info("Funcionário disponível encontrado: {} (ID: {})", funcionario.getName(), funcionario.getId());
                return funcionario;
            } else {
                log.debug("Funcionário {} tem conflito de horário, tentando próximo", funcionario.getId());
            }
        }

        log.warn("Nenhum funcionário disponível encontrado para barbearia {} no horário {}", barbeariaId, dataHora);
        return null;
    }

    @Transactional
    public AgendamentoResponseDTO cancelarAgendamento(Long agendamentoId, String motivo, HttpServletRequest httpRequest) {
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

        String statusAnterior = agendamento.getStatus().name();

        if (isCliente) {
            agendamento.setStatus(StatusAgendamento.CANCELADO_PELO_CLIENTE);
        } else {
            agendamento.setStatus(StatusAgendamento.CANCELADO_PELA_BARBEARIA);
        }

        agendamento.setObservacao(String.format("Cancelado por %s. Motivo: %s",
                isCliente ? "cliente" : "barbearia", motivo));

        Agendamento updated = agendamentoRepository.save(agendamento);

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("agendamentoId", agendamentoId);
        detalhes.put("motivo", motivo);
        detalhes.put("statusAnterior", statusAnterior);
        detalhes.put("statusNovo", agendamento.getStatus().name());
        detalhes.put("canceladoPor", isCliente ? "cliente" : "barbearia");

        logSistemaService.registrarLog(
                "AGENDAMENTO",
                "CANCELADO",
                "Agendamento",
                agendamentoId.toString(),
                String.format("Agendamento %d foi cancelado por %s. Motivo: %s", agendamentoId, isCliente ? "cliente" : "barbearia", motivo),
                detalhes,
                httpRequest
        );

        return convertToResponseDTO(updated);
    }

    @Transactional
    public AgendamentoResponseDTO confirmarAgendamento(Long agendamentoId, HttpServletRequest httpRequest) {
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

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("agendamentoId", agendamentoId);
        detalhes.put("clienteNome", agendamento.getCliente().getName());
        detalhes.put("barbeariaNome", agendamento.getBarbearia().getNome());

        logSistemaService.registrarLog(
                "AGENDAMENTO",
                "CONFIRMADO",
                "Agendamento",
                agendamentoId.toString(),
                String.format("Agendamento %d foi confirmado para cliente %s", agendamentoId, agendamento.getCliente().getName()),
                detalhes,
                httpRequest
        );

        return convertToResponseDTO(updated);
    }


    @Transactional
    public AgendamentoResponseDTO concluirAgendamento(Long agendamentoId, HttpServletRequest httpRequest) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        Usuario usuario = getUsuarioAutenticado();

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

        Map<String, Object> detalhes = new HashMap<>();
        detalhes.put("agendamentoId", agendamentoId);
        detalhes.put("clienteNome", agendamento.getCliente().getName());
        detalhes.put("barbeariaNome", agendamento.getBarbearia().getNome());
        detalhes.put("servicoNome", agendamento.getServico().getNome());
        detalhes.put("valor", agendamento.getServico().getPreco());

        logSistemaService.registrarLog(
                "AGENDAMENTO",
                "CONCLUIDO",
                "Agendamento",
                agendamentoId.toString(),
                String.format("Agendamento %d foi concluído. Cliente: %s, Serviço: %s, Valor: R$ %.2f",
                        agendamentoId, agendamento.getCliente().getName(),
                        agendamento.getServico().getNome(),
                        agendamento.getServico().getPreco()),
                detalhes,
                httpRequest
        );

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