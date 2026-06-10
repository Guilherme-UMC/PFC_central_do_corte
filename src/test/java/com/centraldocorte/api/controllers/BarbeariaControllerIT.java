package com.centraldocorte.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.BarbeariaRequestDTO;
import com.centraldocorte.api.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - BarbeariaController")
class BarbeariaControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BarbeariaRepository barbeariaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenService tokenService;

    private String proprietarioToken;
    private String adminToken;
    private String barbeariaId;

    @BeforeEach
    void setUp() {
        Usuario proprietario = new Usuario();
        proprietario.setName("Proprietário Teste");
        proprietario.setEmail("proprietario@teste.com");
        proprietario.setPassword(passwordEncoder.encode("senha123"));
        proprietario.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        proprietario.setActive(true);
        proprietario = usuarioRepository.save(proprietario);
        proprietarioToken = tokenService.gerarAccessToken(proprietario);

        Usuario admin = new Usuario();
        admin.setName("Admin Teste");
        admin.setEmail("admin@teste.com");
        admin.setPassword(passwordEncoder.encode("senha123"));
        admin.setRole(UsuarioRole.ROLE_ADMIN);
        admin.setActive(true);
        admin = usuarioRepository.save(admin);
        adminToken = tokenService.gerarAccessToken(admin);

        Barbearia barbearia = new Barbearia();
        barbearia.setNome("Barbearia do João");
        barbearia.setDescricao("A melhor barbearia");
        barbearia.setLogradouro("Rua das Flores");
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
    }

    @Test
    @DisplayName("POST /barbearia - Deve criar barbearia")
    void deveCriarBarbearia() throws Exception {
        BarbeariaRequestDTO request = new BarbeariaRequestDTO();
        request.setNome("Nova Barbearia");
        request.setDescricao("Descrição");
        request.setLogradouro("Av. Paulista");
        request.setNumero("1000");
        request.setBairro("Bela Vista");
        request.setCep("01310000");
        request.setCidade("São Paulo");
        request.setUf("SP");
        request.setTelefone("1133333333");

        mockMvc.perform(post("/barbearia")
                        .header("Authorization", "Bearer " + proprietarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Nova Barbearia"));
    }

    @Test
    @DisplayName("GET /barbearia - Deve listar barbearias (público)")
    void deveListarBarbearias() throws Exception {
        mockMvc.perform(get("/barbearia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /barbearia/{id} - Deve buscar barbearia por ID")
    void deveBuscarBarbeariaPorId() throws Exception {
        mockMvc.perform(get("/barbearia/{id}", barbeariaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Barbearia do João"));
    }

    @Test
    @DisplayName("GET /barbearia/minhas - Deve listar barbearias do proprietário")
    void deveListarMinhasBarbearias() throws Exception {
        mockMvc.perform(get("/barbearia/minhas")
                        .header("Authorization", "Bearer " + proprietarioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("PUT /barbearia/{id} - Deve atualizar barbearia")
    void deveAtualizarBarbearia() throws Exception {
        BarbeariaRequestDTO updateRequest = new BarbeariaRequestDTO();
        updateRequest.setNome("Barbearia Atualizada");
        updateRequest.setDescricao("Descrição atualizada");
        updateRequest.setLogradouro("Rua Nova");
        updateRequest.setNumero("456");
        updateRequest.setBairro("Jardins");
        updateRequest.setCep("01414000");
        updateRequest.setCidade("São Paulo");
        updateRequest.setUf("SP");
        updateRequest.setTelefone("1144444444");

        mockMvc.perform(put("/barbearia/{id}", barbeariaId)
                        .header("Authorization", "Bearer " + proprietarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Barbearia Atualizada"));
    }
}