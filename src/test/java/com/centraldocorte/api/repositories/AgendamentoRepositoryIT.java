package com.centraldocorte.api.repositories;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.models.enums.*;
import com.centraldocorte.api.domain.repositories.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - AgendamentoRepository")
class AgendamentoRepositoryIT {

    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private BarbeariaRepository barbeariaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ServicoRepository servicoRepository;

    private Barbearia barbearia;
    private Usuario cliente;
    private Usuario funcionario;
    private Servico servico;

    @BeforeEach
    void setUp() {
        Usuario owner = new Usuario();
        owner.setName("Owner Teste");
        owner.setEmail("owner@teste.com");
        owner.setPassword("senha123");
        owner.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        owner.setActive(true);
        owner = usuarioRepository.save(owner);

        barbearia = new Barbearia();
        barbearia.setNome("Barbearia Teste");
        barbearia.setDescricao("Descrição teste");
        barbearia.setLogradouro("Rua Teste");
        barbearia.setNumero("123");
        barbearia.setBairro("Centro");
        barbearia.setCep("01001000");
        barbearia.setCidade("São Paulo");
        barbearia.setUf("SP");
        barbearia.setTelefone("11999999999");
        barbearia.setAtivo(true);
        barbearia.setOwner(owner);
        barbearia = barbeariaRepository.save(barbearia);

        cliente = new Usuario();
        cliente.setName("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setPassword("senha123");
        cliente.setRole(UsuarioRole.ROLE_CLIENTE);
        cliente.setActive(true);
        cliente = usuarioRepository.save(cliente);

        funcionario = new Usuario();
        funcionario.setName("Funcionario Teste");
        funcionario.setEmail("func@teste.com");
        funcionario.setPassword("senha123");
        funcionario.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        funcionario.setActive(true);
        funcionario = usuarioRepository.save(funcionario);

        servico = new Servico();
        servico.setNome("Corte");
        servico.setPreco(BigDecimal.valueOf(50));
        servico.setDuracaoMinutos(30);
        servico.setBarbearia(barbearia);
        servico.setAtivo(true);
        servico = servicoRepository.save(servico);
    }

    private Agendamento criarAgendamento(Usuario cliente, Usuario funcionario, LocalDateTime dataHora, StatusAgendamento status) {
        Agendamento ag = new Agendamento();
        ag.setBarbearia(barbearia);
        ag.setCliente(cliente);
        ag.setFuncionario(funcionario);
        ag.setServico(servico);
        ag.setDataHora(dataHora);
        ag.setStatus(status);
        return agendamentoRepository.save(ag);
    }

    @Test
    @DisplayName("Deve encontrar agendamentos por barbearia ordenados por data")
    void deveEncontrarAgendamentosPorBarbearia() {
        criarAgendamento(cliente, funcionario, LocalDateTime.now().plusDays(1), StatusAgendamento.CONFIRMADO);
        criarAgendamento(cliente, funcionario, LocalDateTime.now().plusDays(2), StatusAgendamento.CONFIRMADO);

        List<Agendamento> result = agendamentoRepository.findByBarbeariaIdOrderByDataHoraDesc(barbearia.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDataHora()).isAfterOrEqualTo(result.get(1).getDataHora());
    }

    @Test
    @Disabled("Teste desabilitado")
    @DisplayName("Deve contar agendamentos confirmados por funcionário no horário")
    void deveContarAgendamentosConfirmadosPorFuncionarioNoHorario() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        Agendamento ag = criarAgendamento(cliente, funcionario, dataHora, StatusAgendamento.CONFIRMADO);
        agendamentoRepository.flush();

        long count = agendamentoRepository.countAgendamentosPorFuncionarioNoHorario(
                funcionario.getId(), dataHora);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve listar agendamentos futuros de um funcionário")
    void deveListarAgendamentosFuturosDoFuncionario() {
        criarAgendamento(cliente, funcionario, LocalDateTime.now().plusDays(1), StatusAgendamento.CONFIRMADO);
        criarAgendamento(cliente, funcionario, LocalDateTime.now().minusDays(1), StatusAgendamento.CONFIRMADO);

        List<Agendamento> result = agendamentoRepository.findFutureAgendamentosByFuncionarioId(
                funcionario.getId(), LocalDateTime.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDataHora()).isAfter(LocalDateTime.now());
    }
}