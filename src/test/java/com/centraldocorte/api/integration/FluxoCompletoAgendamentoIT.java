package com.centraldocorte.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.models.enums.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.AgendamentoRequestDTO;
import com.centraldocorte.api.services.TokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@DisplayName("Testes End-to-End - Fluxo Completo de Agendamento")
class FluxoCompletoAgendamentoIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private BarbeariaRepository barbeariaRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenService tokenService;

    private String clienteToken;
    private String proprietarioToken;
    private String funcionarioToken;
    private String barbeariaId;
    private String servicoId;
    private String funcionarioId;

    @BeforeEach
    void setUp() {
        Usuario proprietario = new Usuario();
        proprietario.setName("Proprietário Barbearia");
        proprietario.setEmail("proprietario@barbearia.com");
        proprietario.setPassword(passwordEncoder.encode("senha123"));
        proprietario.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        proprietario.setActive(true);
        proprietario = usuarioRepository.save(proprietario);
        proprietarioToken = tokenService.gerarAccessToken(proprietario);

        Barbearia barbearia = new Barbearia();
        barbearia.setNome("Barbearia Central");
        barbearia.setDescricao("A melhor barbearia da cidade");
        barbearia.setLogradouro("Av. Principal");
        barbearia.setNumero("100");
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
        servico.setNome("Corte de Cabelo");
        servico.setDescricao("Corte tradicional");
        servico.setPreco(BigDecimal.valueOf(50.00));
        servico.setDuracaoMinutos(30);
        servico.setBarbearia(barbearia);
        servico.setAtivo(true);
        servico = servicoRepository.save(servico);
        servicoId = servico.getId();

        Usuario funcionario = new Usuario();
        funcionario.setName("Barbeiro João");
        funcionario.setEmail("joao@barbearia.com");
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
        funcionarioToken = tokenService.gerarAccessToken(funcionario);

        Usuario cliente = new Usuario();
        cliente.setName("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setPassword(passwordEncoder.encode("senha123"));
        cliente.setRole(UsuarioRole.ROLE_CLIENTE);
        cliente.setActive(true);
        cliente = usuarioRepository.save(cliente);
        clienteToken = tokenService.gerarAccessToken(cliente);
    }

    @Test
    @DisplayName("Fluxo Completo: Cliente agenda -> Proprietário confirma -> Funcionário conclui")
    void fluxoCompletoAgendamento() throws Exception {
        LocalDateTime dataAgendamento = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);

        AgendamentoRequestDTO request = new AgendamentoRequestDTO();
        request.setBarbeariaId(barbeariaId);
        request.setServicoId(servicoId);
        request.setFuncionarioId(funcionarioId);
        request.setDataHora(dataAgendamento);
        request.setObservacao("Prefiro horário pela manhã");

        String agendamentoResponse = mockMvc.perform(post("/api/agendamentos")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("Aguardando confirmação"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long agendamentoId = objectMapper.readTree(agendamentoResponse).get("id").asLong();

        mockMvc.perform(put("/api/agendamentos/{id}/confirmar", agendamentoId)
                        .header("Authorization", "Bearer " + proprietarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmado"));

        mockMvc.perform(put("/api/agendamentos/{id}/concluir", agendamentoId)
                        .header("Authorization", "Bearer " + funcionarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Concluído"));
    }
}