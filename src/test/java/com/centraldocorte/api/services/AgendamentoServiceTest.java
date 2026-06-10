package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.models.enums.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AgendamentoService")
class AgendamentoServiceTest {

    @Mock private AgendamentoRepository agendamentoRepository;
    @Mock private BarbeariaRepository barbeariaRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    @Mock private HorarioService horarioService;
    @Mock private DisponibilidadeService disponibilidadeService;
    @Mock private LogSistemaService logSistemaService;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Usuario clienteAutenticado;
    private Usuario funcionario;
    private Usuario owner;
    private Barbearia barbeariaAtiva;
    private Servico servico;

    @BeforeEach
    void setUp() {
        clienteAutenticado = new Usuario();
        clienteAutenticado.setId("cliente-123");
        clienteAutenticado.setEmail("cliente@teste.com");
        clienteAutenticado.setName("Cliente Teste");
        clienteAutenticado.setRole(UsuarioRole.ROLE_CLIENTE);
        clienteAutenticado.setActive(true);

        funcionario = new Usuario();
        funcionario.setId("func-456");
        funcionario.setName("Barbeiro João");
        funcionario.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        funcionario.setActive(true);

        owner = new Usuario();
        owner.setId("owner-789");
        owner.setName("Proprietário");
        owner.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        owner.setActive(true);

        barbeariaAtiva = new Barbearia();
        barbeariaAtiva.setId("barb-001");
        barbeariaAtiva.setNome("Barbearia Teste");
        barbeariaAtiva.setAtivo(true);
        barbeariaAtiva.setOwner(owner);

        servico = new Servico();
        servico.setId("serv-001");
        servico.setNome("Corte de Cabelo");
        servico.setPreco(BigDecimal.valueOf(50.0));
        servico.setDuracaoMinutos(30);
        servico.setBarbearia(barbeariaAtiva);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cliente@teste.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve criar agendamento com sucesso quando funcionário específico for escolhido")
    void deveCriarAgendamentoComFuncionarioEspecifico() {
        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(clienteAutenticado));
        when(barbeariaRepository.findById("barb-001")).thenReturn(Optional.of(barbeariaAtiva));
        when(servicoRepository.findById("serv-001")).thenReturn(Optional.of(servico));
        when(usuarioRepository.findById("func-456")).thenReturn(Optional.of(funcionario));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(
                eq("func-456"), eq("barb-001"))).thenReturn(true);
        when(disponibilidadeService.isHorarioDisponivelParaFuncionario(eq("func-456"), any(LocalDateTime.class)))
                .thenReturn(true);

        Agendamento agendamentoSalvo = new Agendamento();
        agendamentoSalvo.setId(1L);
        agendamentoSalvo.setCliente(clienteAutenticado);
        agendamentoSalvo.setBarbearia(barbeariaAtiva);
        agendamentoSalvo.setServico(servico);
        agendamentoSalvo.setFuncionario(funcionario);
        agendamentoSalvo.setStatus(StatusAgendamento.PENDENTE);
        agendamentoSalvo.setDataHora(LocalDateTime.now().plusDays(2));

        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamentoSalvo);

        AgendamentoRequestDTO request = new AgendamentoRequestDTO();
        request.setBarbeariaId("barb-001");
        request.setServicoId("serv-001");
        request.setFuncionarioId("func-456");
        request.setDataHora(LocalDateTime.now().plusDays(2));
        request.setObservacao("Prefiro horário pela manhã");

        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(request, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBarbeariaNome()).isEqualTo("Barbearia Teste");

        verify(logSistemaService).registrarLog(eq("AGENDAMENTO"), eq("CRIADO"), any(), any(), any(), any(), eq(httpRequest));
    }

    @Test
    @DisplayName("Deve lançar exceção quando funcionário não pertence à barbearia")
    void deveLancarExcecaoQuandoFuncionarioNaoPertenceABarbearia() {
        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(clienteAutenticado));
        when(barbeariaRepository.findById("barb-001")).thenReturn(Optional.of(barbeariaAtiva));
        when(servicoRepository.findById("serv-001")).thenReturn(Optional.of(servico));
        when(usuarioRepository.findById("func-456")).thenReturn(Optional.of(funcionario));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(
                eq("func-456"), eq("barb-001"))).thenReturn(false);

        AgendamentoRequestDTO request = new AgendamentoRequestDTO();
        request.setBarbeariaId("barb-001");
        request.setServicoId("serv-001");
        request.setFuncionarioId("func-456");
        request.setDataHora(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> agendamentoService.criarAgendamento(request, httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Este funcionário não pertence à barbearia selecionada");
    }

    @Test
    @DisplayName("Deve cancelar agendamento com sucesso")
    void deveCancelarAgendamentoComSucesso() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(clienteAutenticado);
        agendamento.setBarbearia(barbeariaAtiva);
        agendamento.setServico(servico);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setDataHora(LocalDateTime.now().plusDays(2));

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(clienteAutenticado));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));

        AgendamentoResponseDTO response = agendamentoService.cancelarAgendamento(1L, "Mudei de ideia", httpRequest);

        assertThat(response).isNotNull();
        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO_PELO_CLIENTE);

        verify(logSistemaService).registrarLog(eq("AGENDAMENTO"), eq("CANCELADO"), any(), any(), any(), any(), eq(httpRequest));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar agendamento com menos de 24h")
    void deveLancarExcecaoAoCancelarComMenosDe24h() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(clienteAutenticado);
        agendamento.setBarbearia(barbeariaAtiva);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setDataHora(LocalDateTime.now().plusHours(12));

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(usuarioRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(clienteAutenticado));

        assertThatThrownBy(() -> agendamentoService.cancelarAgendamento(1L, "Motivo", httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cancelamentos devem ser feitos com pelo menos 24 horas de antecedência");
    }
}