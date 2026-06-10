package com.centraldocorte.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.models.enums.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - AgendamentoController")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AgendamentoControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private BarbeariaRepository barbeariaRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenService tokenService;

    private String clienteToken;
    private String funcionarioToken;
    private String barbeariaId;
    private String servicoId;
    private String funcionarioId;

    @BeforeEach
    void setUp() {
        Usuario proprietario = new Usuario();
        proprietario.setName("Proprietário");
        proprietario.setEmail("proprietario@teste.com");
        proprietario.setPassword(passwordEncoder.encode("senha123"));
        proprietario.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        proprietario.setActive(true);
        proprietario = usuarioRepository.save(proprietario);

        Barbearia barbearia = new Barbearia();
        barbearia.setNome("Barbearia Teste");
        barbearia.setLogradouro("Rua Teste");
        barbearia.setNumero("123");
        barbearia.setBairro("Centro");
        barbearia.setCep("01001000");
        barbearia.setCidade("São Paulo");
        barbearia.setUf("SP");
        barbearia.setTelefone("11999999999");
        barbearia.setAtivo(true);
        barbearia.setOwner(proprietario);
        barbearia = barbeariaRepository.save(barbearia);
        barbeariaId = barbearia.getId();

        Servico servico = new Servico();
        servico.setNome("Corte");
        servico.setPreco(BigDecimal.valueOf(50));
        servico.setDuracaoMinutos(30);
        servico.setBarbearia(barbearia);
        servico.setAtivo(true);
        servico = servicoRepository.save(servico);
        servicoId = servico.getId();

        Usuario funcionario = new Usuario();
        funcionario.setName("Barbeiro");
        funcionario.setEmail("barbeiro@teste.com");
        funcionario.setPassword(passwordEncoder.encode("senha123"));
        funcionario.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        funcionario.setActive(true);
        funcionario = usuarioRepository.save(funcionario);
        funcionarioId = funcionario.getId();

        FuncionarioBarbearia vinculo = FuncionarioBarbearia.builder()
                .funcionario(funcionario)
                .barbearia(barbearia)
                .ativo(true)
                .disponivel(true)
                .build();
        funcionarioBarbeariaRepository.save(vinculo);

        Usuario cliente = new Usuario();
        cliente.setName("Cliente");
        cliente.setEmail("cliente@teste.com");
        cliente.setPassword(passwordEncoder.encode("senha123"));
        cliente.setRole(UsuarioRole.ROLE_CLIENTE);
        cliente.setActive(true);
        cliente = usuarioRepository.save(cliente);
        clienteToken = tokenService.gerarAccessToken(cliente);
        funcionarioToken = tokenService.gerarAccessToken(funcionario);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/agendamentos - Deve criar agendamento")
    @Disabled
    void deveCriarAgendamento() throws Exception {
        AgendamentoRequestDTO request = new AgendamentoRequestDTO();
        request.setBarbeariaId(barbeariaId);
        request.setServicoId(servicoId);
        request.setFuncionarioId(funcionarioId);
        request.setDataHora(LocalDateTime.now().plusDays(2));
        request.setObservacao("Teste");

        mockMvc.perform(post("/api/agendamentos")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @Disabled("Teste desabilitado")
    @WithMockUser(roles = "FUNCIONARIO")
    @DisplayName("GET /api/agendamentos/funcionario/meus - Deve listar agendamentos")
    void deveListarAgendamentosDoFuncionario() throws Exception {
        mockMvc.perform(get("/api/agendamentos/funcionario/meus")
                        .header("Authorization", "Bearer " + funcionarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}