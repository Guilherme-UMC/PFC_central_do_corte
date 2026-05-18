package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.dto.AgendamentoResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamentoService - Testes Unitários")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BarbeariaRepository barbeariaRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;

    @Mock
    private HorarioFuncionamentoService horarioFuncionamentoService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Usuario clientePadrao;
    private Usuario funcionarioPadrao;
    private Barbearia barbeariaPadrao;
    private Servico servicoPadrao;
    private AgendamentoRequestDTO requestPadrao;

    @BeforeEach
    void configurarCenarioBase() {
        clientePadrao = criarCliente("cliente-1", "João Silva");
        funcionarioPadrao = criarFuncionario("funcionario-1", "Pedro Barbeiro");
        barbeariaPadrao = criarBarbearia("barbearia-1", "Barbearia do Ze");
        servicoPadrao = criarServico("servico-1", "Corte Simples", BigDecimal.valueOf(30), 30);

        requestPadrao = new AgendamentoRequestDTO();
        requestPadrao.setDataHora(LocalDateTime.now().plusDays(1));
        requestPadrao.setBarbeariaId("barbearia-1");
        requestPadrao.setFuncionarioId("funcionario-1");
        requestPadrao.setServicosIds(List.of("servico-1"));
    }

    @Test
    @DisplayName("Deve criar agendamento com sucesso quando todos os dados são válidos")
    void deveCriarAgendamentoComSucesso() {
        configurarMocksParaCenarioSucesso();

        Agendamento agendamentoEsperado = new Agendamento();
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamentoEsperado);

        Agendamento resultado = agendamentoService.criarAgendamento("cliente-1", requestPadrao);

        assertThat(resultado).isNotNull();
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cliente não é encontrado")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        when(usuarioRepository.findById("cliente-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-inexistente", requestPadrao))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando barbearia não é encontrada")
    void deveLancarExcecaoQuandoBarbeariaNaoExiste() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Barbearia não encontrada");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando funcionário não é encontrado")
    void deveLancarExcecaoQuandoFuncionarioNaoExiste() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.of(barbeariaPadrao));
        when(usuarioRepository.findById("funcionario-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Funcionário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando funcionário não pertence à barbearia")
    void deveLancarExcecaoQuandoFuncionarioNaoPertenceABarbearia() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.of(barbeariaPadrao));
        when(usuarioRepository.findById("funcionario-1")).thenReturn(Optional.of(funcionarioPadrao));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(anyString(), anyString()))
                .thenReturn(false);

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Funcionário não pertence a esta barbearia");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando horário está fora do expediente")
    void deveLancarExcecaoQuandoHorarioForaDoExpediente() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.of(barbeariaPadrao));
        when(usuarioRepository.findById("funcionario-1")).thenReturn(Optional.of(funcionarioPadrao));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(anyString(), anyString()))
                .thenReturn(true);
        when(horarioFuncionamentoService.isHorarioValido(anyString(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Horário fora do expediente");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando nenhum serviço válido é encontrado")
    void deveLancarExcecaoQuandoNenhumServicoValido() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.of(barbeariaPadrao));
        when(usuarioRepository.findById("funcionario-1")).thenReturn(Optional.of(funcionarioPadrao));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(anyString(), anyString()))
                .thenReturn(true);
        when(horarioFuncionamentoService.isHorarioValido(anyString(), any(), any())).thenReturn(true);
        when(servicoRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nenhum serviço válido encontrado");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando há conflito de horário com outro agendamento")
    void deveLancarExcecaoQuandoFuncionarioJaPossuiAgendamento() {
        configurarMocksParaCenarioSucesso();

        Agendamento agendamentoExistente = new Agendamento();
        when(agendamentoRepository.findByFuncionarioIdAndDataHoraBetween(anyString(), any(), any()))
                .thenReturn(List.of(agendamentoExistente));

        assertThatThrownBy(() -> agendamentoService.criarAgendamento("cliente-1", requestPadrao))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Funcionário já possui agendamento neste horário");
    }

    @Test
    @DisplayName("Deve retornar lista de agendamentos do cliente ordenados por data")
    void deveListarAgendamentosDoCliente() {
        Agendamento agendamento = montarAgendamentoCompleto();
        when(agendamentoRepository.findByClienteIdOrderByDataHoraDesc("cliente-1"))
                .thenReturn(List.of(agendamento));

        List<AgendamentoResponseDTO> resultado = agendamentoService.listarAgendamentosCliente("cliente-1");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getClienteNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando cliente não possui agendamentos")
    void deveRetornarListaVaziaQuandoClienteSemAgendamentos() {
        when(agendamentoRepository.findByClienteIdOrderByDataHoraDesc("cliente-1"))
                .thenReturn(Collections.emptyList());

        List<AgendamentoResponseDTO> resultado = agendamentoService.listarAgendamentosCliente("cliente-1");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista de agendamentos da barbearia ordenados por data")
    void deveListarAgendamentosDaBarbearia() {
        Agendamento agendamento = montarAgendamentoCompleto();
        when(agendamentoRepository.findByBarbeariaIdOrderByDataHoraAsc("barbearia-1"))
                .thenReturn(List.of(agendamento));

        List<AgendamentoResponseDTO> resultado = agendamentoService.listarAgendamentosBarbearia("barbearia-1");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getBarbeariaNome()).isEqualTo("Barbearia do Ze");
    }

    @Test
    @DisplayName("Deve atualizar o status do agendamento para CONFIRMADO com sucesso")
    void deveAtualizarStatusParaConfirmado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        when(agendamentoRepository.findById("agendamento-1")).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any())).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.atualizarStatusAgendamento("agendamento-1", "CONFIRMADO");

        assertThat(resultado.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando status informado é inválido")
    void deveLancarExcecaoQuandoStatusInvalido() {
        Agendamento agendamento = new Agendamento();
        when(agendamentoRepository.findById("agendamento-1")).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> agendamentoService.atualizarStatusAgendamento("agendamento-1", "STATUS_INVALIDO"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status inválido");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando agendamento não é encontrado para atualizar")
    void deveLancarExcecaoQuandoAgendamentoNaoEncontrado() {
        when(agendamentoRepository.findById("agendamento-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                agendamentoService.atualizarStatusAgendamento("agendamento-inexistente", "CONFIRMADO"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agendamento não encontrado");
    }

    private void configurarMocksParaCenarioSucesso() {
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(clientePadrao));
        when(barbeariaRepository.findById("barbearia-1")).thenReturn(Optional.of(barbeariaPadrao));
        when(usuarioRepository.findById("funcionario-1")).thenReturn(Optional.of(funcionarioPadrao));
        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(anyString(), anyString()))
                .thenReturn(true);
        when(horarioFuncionamentoService.isHorarioValido(anyString(), any(), any())).thenReturn(true);
        when(servicoRepository.findAllById(anyList())).thenReturn(List.of(servicoPadrao));
        when(agendamentoRepository.findByFuncionarioIdAndDataHoraBetween(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
    }

    private Agendamento montarAgendamentoCompleto() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId("agendamento-1");
        agendamento.setCliente(clientePadrao);
        agendamento.setBarbearia(barbeariaPadrao);
        agendamento.setFuncionario(funcionarioPadrao);
        agendamento.setDataHora(LocalDateTime.now().plusDays(1));
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setServicos(List.of(servicoPadrao));
        agendamento.setCriadoEm(LocalDateTime.now());
        return agendamento;
    }

    private Usuario criarCliente(String id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setName(nome);
        usuario.setEmail(nome.toLowerCase().replace(" ", ".") + "@email.com");
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        return usuario;
    }

    private Usuario criarFuncionario(String id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setName(nome);
        usuario.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        usuario.setActive(true);
        return usuario;
    }

    private Barbearia criarBarbearia(String id, String nome) {
        Barbearia barbearia = new Barbearia();
        barbearia.setId(id);
        barbearia.setNome(nome);
        barbearia.setAtivo(true);
        return barbearia;
    }

    private Servico criarServico(String id, String nome, BigDecimal preco, int duracaoMinutos) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setNome(nome);
        servico.setPreco(preco);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setBarbearia(barbeariaPadrao);
        return servico;
    }
}
