package com.centraldocorte.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import org.junit.jupiter.api.*;
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
@DisplayName("Testes de Integração - AuthController")
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register - Deve registrar novo cliente")
    void deveRegistrarNovoCliente() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Usuario Teste", "teste@email.com", "senha123", "11999999999"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar erro quando email já existe")
    void deveRetornarErroQuandoEmailJaExiste() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setName("Existente");
        usuario.setEmail("teste@email.com");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        usuarioRepository.save(usuario);

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Usuario Teste", "teste@email.com", "senha123", "11999999999"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Email já cadastrado: teste@email.com"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve autenticar com sucesso")
    void deveAutenticarComSucesso() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setName("Usuario");
        usuario.setEmail("login@teste.com");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        usuarioRepository.save(usuario);

        LoginRequestDTO login = new LoginRequestDTO("login@teste.com", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar erro com senha incorreta")
    void deveRetornarErroComSenhaIncorreta() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setName("Usuario");
        usuario.setEmail("login@teste.com");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        usuarioRepository.save(usuario);

        LoginRequestDTO login = new LoginRequestDTO("login@teste.com", "senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Email ou senha inválidos"));
    }
}