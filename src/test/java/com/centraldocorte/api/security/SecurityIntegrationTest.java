package com.centraldocorte.api.security;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Segurança - Autorização de Endpoints")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /barbearia - endpoint público deve ser acessível sem token")
    void endpointPublicoDeveSerAcessivelSemToken() throws Exception {
        mockMvc.perform(get("/barbearia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/agendamentos - endpoint protegido retorna 403 sem token")
    void endpointProtegidoBloqueiaSemToken() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GET /api/admin/logs - cliente NÃO deve acessar endpoint de ADMIN")
    void clienteNaoAcessaEndpointAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/logs - admin deve acessar endpoint de logs")
    void adminAcessaEndpointLogs() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /barbearia/buscar-cep/01001000 - endpoint público de CEP")
    void endpointCepPublico() throws Exception {
        mockMvc.perform(get("/barbearia/buscar-cep/01001000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    @Disabled("Teste desabilitado - problema com autenticação no H2")
    @DisplayName("Funcionário acessa seus agendamentos")
    void funcionarioAcessaSeusAgendamentos() throws Exception {
        mockMvc.perform(get("/api/agendamentos/funcionario/meus"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("POST /api/agendamentos - cliente cria agendamento (validação de dados)")
    void clienteCriaAgendamentoComDadosInvalidos() throws Exception {
        String requestBody = """
                {
                    "barbeariaId": "",
                    "servicoId": "",
                    "dataHora": null
                }
                """;

        mockMvc.perform(post("/api/agendamentos")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}