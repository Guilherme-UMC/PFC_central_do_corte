package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ServicoRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
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
    private final HorarioService horarioService;
    private final DisponibilidadeService disponibilidadeService;  // ← NOVA DEPENDÊNCIA

    // ================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ================================

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

        // Tratamento do preço (BigDecimal para Double)
        BigDecimal preco = agendamento.getServico().getPreco();
        dto.setServicoPreco(preco != null ? preco.doubleValue() : 0.0);

        // Duração do serviço
        dto.setServicoDuracao(agendamento.getServico().getDuracaoMinutos() != null ?
                agendamento.getServico().getDuracaoMinutos() : 30);

        dto.setDataHora(agendamento.getDataHora());
        dto.setStatus(agendamento.getStatus().getDescricao());
        dto.setObservacao(agendamento.getObservacao());
        dto.setCriadoEm(agendamento.getCriadoEm());
        return dto;
    }

    // ================================
    // MÉTODOS PÚBLICOS
    // ================================

    /**
     * Cria um novo agendamento
     */
    @Transactional
    public AgendamentoResponseDTO criarAgendamento(AgendamentoRequestDTO request) {
        // 1. Validar cliente
        Usuario cliente = getUsuarioAutenticado();
        if (!cliente.getRole().equals("ROLE_CLIENTE")) {
            throw new BusinessException("Apenas clientes podem realizar agendamentos");
        }

        // 2. Validar barbearia
        Barbearia barbearia = barbeariaRepository.findById(request.getBarbeariaId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        if (!barbearia.getAtivo()) {
            throw new BusinessException("Esta barbearia está inativa no momento");
        }

        // 3. Validar serviço
        Servico servico = servicoRepository.findById(request.getServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        // Verificar se o serviço pertence à barbearia
        if (!servico.getBarbearia().getId().equals(barbearia.getId())) {
            throw new BusinessException("Este serviço não pertence à barbearia selecionada");
        }

        // 4. Validar horário
        if (request.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível agendar para data/hora passada");
        }

        if (!horarioService.isBarbeariaAberta(barbearia.getId(), request.getDataHora())) {
            throw new BusinessException("A barbearia não está aberta neste horário");
        }

        // ✅ USANDO DisponibilidadeService em vez do método interno
        if (!disponibilidadeService.isHorarioDisponivel(barbearia.getId(), request.getDataHora())) {
            throw new BusinessException("Este horário já está ocupado");
        }

        // 5. Criar agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setBarbearia(barbearia);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHora(request.getDataHora());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setObservacao(request.getObservacao());

        Agendamento saved = agendamentoRepository.save(agendamento);
        return convertToResponseDTO(saved);
    }

    /**
     * Cancela um agendamento existente
     */
    @Transactional
    public AgendamentoResponseDTO cancelarAgendamento(Long agendamentoId, String motivo) {
        // 1. Buscar agendamento
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // 2. Validar permissão
        Usuario usuario = getUsuarioAutenticado();
        boolean isCliente = agendamento.getCliente().getId().equals(usuario.getId());
        boolean isDonoBarbearia = agendamento.getBarbearia().getOwner().getId().equals(usuario.getId());

        if (!isCliente && !isDonoBarbearia) {
            throw new UnauthorizedException("Você não tem permissão para cancelar este agendamento");
        }

        // 3. Validar status
        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Não é possível cancelar um agendamento já concluído");
        }

        // 4. Validar prazo (2 horas de antecedência)
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limiteCancelamento = agendamento.getDataHora().minusHours(2);

        if (agendamento.getDataHora().isBefore(agora)) {
            throw new BusinessException("Não é possível cancelar um agendamento que já passou");
        }

        if (agora.isAfter(limiteCancelamento)) {
            throw new BusinessException("Cancelamentos devem ser feitos com pelo menos 2 horas de antecedência");
        }

        // 5. Cancelar
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

    /**
     * Confirma um agendamento (apenas dono da barbearia)
     */
    @Transactional
    public AgendamentoResponseDTO confirmarAgendamento(Long agendamentoId) {
        // 1. Buscar agendamento
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // 2. Validar permissão
        Usuario usuario = getUsuarioAutenticado();
        if (!agendamento.getBarbearia().getOwner().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("Apenas o proprietário da barbearia pode confirmar agendamentos");
        }

        // 3. Validar status
        if (agendamento.getStatus() != StatusAgendamento.PENDENTE) {
            throw new BusinessException("Apenas agendamentos pendentes podem ser confirmados");
        }

        // 4. Confirmar
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return convertToResponseDTO(updated);
    }

    /**
     * Conclui um agendamento (após o serviço ser realizado)
     */
    @Transactional
    public AgendamentoResponseDTO concluirAgendamento(Long agendamentoId) {
        // 1. Buscar agendamento
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        // 2. Validar permissão
        Usuario usuario = getUsuarioAutenticado();
        if (!agendamento.getBarbearia().getOwner().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("Apenas o proprietário da barbearia pode concluir agendamentos");
        }

        // 3. Validar status
        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser concluídos");
        }

        // 4. Concluir
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        Agendamento updated = agendamentoRepository.save(agendamento);

        return convertToResponseDTO(updated);
    }

    /**
     * Lista todos os agendamentos do cliente autenticado
     */
    public List<AgendamentoResponseDTO> getAgendamentosDoCliente() {
        Usuario cliente = getUsuarioAutenticado();

        return agendamentoRepository.findByClienteIdOrderByDataHoraDesc(cliente.getId())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os agendamentos de uma barbearia (apenas dono ou admin)
     */
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

    /**
     * Lista os agendamentos do dia para uma barbearia
     */
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
}