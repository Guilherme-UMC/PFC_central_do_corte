package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TokenService.class})
@TestPropertySource(properties = {
        "api.security.token.secret=chaveSuperSecretaParaTestesCom32CaracteresOuMais123"
})
@DisplayName("Testes Unitários - TokenService")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId("user-123");
        usuario.setEmail("teste@teste.com");
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
    }

    @Test
    void deveGerarAccessTokenValido() {
        String token = tokenService.gerarAccessToken(usuario);

        assertThat(token).isNotBlank();

        String email = tokenService.validarAccessToken(token);
        assertThat(email).isEqualTo("teste@teste.com");

        String userId = tokenService.extrairUsuarioId(token);
        assertThat(userId).isEqualTo("user-123");

        String role = tokenService.extrairRole(token);
        assertThat(role).isEqualTo("ROLE_CLIENTE");
    }
}