package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.LoginResponseDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import com.centraldocorte.api.dto.RegisterResponseDTO;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AuthService")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenService tokenService;
    @Mock private LogSistemaService logSistemaService;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private AuthService authService;

    private Usuario usuarioAtivo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = new Usuario();
        usuarioAtivo.setId("user-123");
        usuarioAtivo.setName("Usuário Teste");
        usuarioAtivo.setEmail("teste@email.com");
        usuarioAtivo.setPassword("senhaCriptografada");
        usuarioAtivo.setRole(UsuarioRole.ROLE_CLIENTE);
        usuarioAtivo.setActive(true);
    }

    @Test
    @DisplayName("Deve autenticar usuário com sucesso")
    void deveAutenticarUsuarioComSucesso() {
        LoginRequestDTO request = new LoginRequestDTO("teste@email.com", "senha123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuarioAtivo);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(tokenService.gerarAccessToken(usuarioAtivo)).thenReturn("access-token-123");
        when(tokenService.gerarRefreshToken(usuarioAtivo)).thenReturn("refresh-token-456");

        LoginResponseDTO response = authService.autenticarUsuario(request, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.userId()).isEqualTo("user-123");

        verify(logSistemaService).registrarLog(
                eq("LOGIN"), eq("LOGIN"), eq("Usuario"), eq("user-123"),
                anyString(), any(), eq(httpRequest));
    }

    @Test
    @DisplayName("Deve lançar exceção quando credenciais são inválidas")
    void deveLancarExcecaoQuandoCredenciaisInvalidas() {
        LoginRequestDTO request = new LoginRequestDTO("teste@email.com", "senhaErrada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.autenticarUsuario(request, httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email ou senha inválidos");
    }

    @Test
    @DisplayName("Deve registrar novo cliente com sucesso")
    void deveRegistrarNovoClienteComSucesso() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Novo Cliente", "novo@email.com", "senha123", "11999999999"
        );

        when(usuarioRepository.existsByEmailIgnoreCase("novo@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId("new-user-789");
            return usuario;
        });

        RegisterResponseDTO response = authService.registrarUsuario(request, UsuarioRole.ROLE_CLIENTE, httpRequest);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo("new-user-789");
        assertThat(response.name()).isEqualTo("Novo Cliente");
        assertThat(response.email()).isEqualTo("novo@email.com");
        assertThat(response.role()).isEqualTo("ROLE_CLIENTE");

        verify(logSistemaService).registrarLog(
                eq("USUARIO"), eq("CRIADO"), eq("Usuario"), eq("new-user-789"),
                anyString(), any(), eq(httpRequest));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar com email já existente")
    void deveLancarExcecaoQuandoEmailJaExiste() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Usuário Duplicado", "existente@email.com", "senha123", "11999999999"
        );

        when(usuarioRepository.existsByEmailIgnoreCase("existente@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrarUsuario(request, UsuarioRole.ROLE_CLIENTE, httpRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email já cadastrado: existente@email.com");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve registrar proprietário de barbearia")
    void deveRegistrarProprietarioBarbearia() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Dono da Barbearia", "dono@barbearia.com", "senha123", "11988888888"
        );

        when(usuarioRepository.existsByEmailIgnoreCase("dono@barbearia.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId("owner-123");
            return usuario;
        });

        RegisterResponseDTO response = authService.registrarUsuario(request, UsuarioRole.ROLE_BARBEARIA_ADM, httpRequest);

        assertThat(response.role()).isEqualTo("ROLE_BARBEARIA_ADM");
        assertThat(response.userId()).isEqualTo("owner-123");

        verify(logSistemaService).registrarLog(
                eq("USUARIO"), eq("CRIADO"), eq("Usuario"), eq("owner-123"),
                anyString(), any(), eq(httpRequest));
    }

    @Test
    @DisplayName("Deve renovar access token com refresh token válido")
    void deveRenovarAccessTokenComRefreshTokenValido() {
        String refreshToken = "refresh-token-valido";
        when(tokenService.validarRefreshToken(refreshToken)).thenReturn("teste@email.com");
        when(usuarioRepository.findByEmailIgnoreCase("teste@email.com")).thenReturn(Optional.of(usuarioAtivo));
        when(tokenService.gerarAccessToken(usuarioAtivo)).thenReturn("novo-access-token");

        LoginResponseDTO response = authService.renovarToken(refreshToken);

        assertThat(response.token()).isEqualTo("novo-access-token");
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token é inválido")
    void deveLancarExcecaoQuandoRefreshTokenInvalido() {
        String refreshToken = "refresh-token-invalido";
        when(tokenService.validarRefreshToken(refreshToken)).thenReturn(null);

        assertThatThrownBy(() -> authService.renovarToken(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido ou expirado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário do refresh token está inativo")
    void deveLancarExcecaoQuandoUsuarioDoRefreshTokenInativo() {
        String refreshToken = "refresh-token-valido";
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setEmail("inativo@email.com");
        usuarioInativo.setActive(false);

        when(tokenService.validarRefreshToken(refreshToken)).thenReturn("inativo@email.com");
        when(usuarioRepository.findByEmailIgnoreCase("inativo@email.com")).thenReturn(Optional.of(usuarioInativo));

        assertThatThrownBy(() -> authService.renovarToken(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuário inativo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário do refresh token não existe")
    void deveLancarExcecaoQuandoUsuarioDoRefreshTokenNaoExiste() {
        String refreshToken = "refresh-token-valido";
        when(tokenService.validarRefreshToken(refreshToken)).thenReturn("naoexiste@email.com");
        when(usuarioRepository.findByEmailIgnoreCase("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.renovarToken(refreshToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}