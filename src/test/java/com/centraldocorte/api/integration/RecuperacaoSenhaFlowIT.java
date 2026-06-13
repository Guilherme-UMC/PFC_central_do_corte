package com.centraldocorte.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.RecuperacaoSenhaToken;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.RecuperacaoSenhaTokenRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.EsqueciSenhaRequestDTO;
import com.centraldocorte.api.dto.RedefinirSenhaRequestDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Disabled("Teste desabilitado - requer servidor de email")
@DisplayName("Testes End-to-End - Fluxo de Recuperação de Senha")
class RecuperacaoSenhaFlowIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RecuperacaoSenhaTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String emailUsuario;
    private String senhaOriginal;

    @BeforeEach
    void setUp() {
        emailUsuario = "usuario@teste.com";
        senhaOriginal = "senhaOriginal123";

        Usuario usuario = new Usuario();
        usuario.setName("Usuario Teste");
        usuario.setEmail(emailUsuario);
        usuario.setPassword(passwordEncoder.encode(senhaOriginal));
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        usuarioRepository.save(usuario);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Fluxo Completo: Solicitar redefinição -> Redefinir senha")
    void fluxoCompletoRecuperacaoSenha() throws Exception {
        EsqueciSenhaRequestDTO esqueciSenhaRequest = new EsqueciSenhaRequestDTO(emailUsuario);

        mockMvc.perform(post("/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(esqueciSenhaRequest)))
                .andExpect(status().isOk());

        var tokens = tokenRepository.findAll();
        assertThat(tokens).hasSize(1);
        String token = tokens.get(0).getToken();

        mockMvc.perform(get("/auth/validar-token")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        String novaSenha = "novaSenha456";
        RedefinirSenhaRequestDTO redefinirRequest = new RedefinirSenhaRequestDTO(token, novaSenha);

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redefinirRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso! Faça login com sua nova senha."));

        Usuario usuarioAtualizado = usuarioRepository.findByEmailIgnoreCase(emailUsuario).orElseThrow();
        assertThat(passwordEncoder.matches(novaSenha, usuarioAtualizado.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(senhaOriginal, usuarioAtualizado.getPassword())).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void deveRejeitarTokenInvalido() throws Exception {
        RedefinirSenhaRequestDTO request = new RedefinirSenhaRequestDTO("token-invalido-123", "novaSenha");

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Token inválido ou já utilizado"));
    }
}