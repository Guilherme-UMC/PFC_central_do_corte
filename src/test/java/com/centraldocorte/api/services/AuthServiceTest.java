package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.LoginResponseDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import com.centraldocorte.api.dto.RegisterResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioPadrao;

    @BeforeEach
    void configurarCenarioBase() {
        usuarioPadrao = new Usuario();
        usuarioPadrao.setId("usuario-1");
        usuarioPadrao.setName("João Silva");
        usuarioPadrao.setEmail("joao@email.com");
        usuarioPadrao.setPassword("senhaCodificada");
        usuarioPadrao.setRole(UsuarioRole.ROLE_CLIENTE);
        usuarioPadrao.setActive(true);
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token JWT com sucesso")
    void deveAutenticarUsuarioComSucesso() {
        Authentication autenticacaoMock = mock(Authentication.class);
        when(autenticacaoMock.getPrincipal()).thenReturn(usuarioPadrao);
        when(authenticationManager.authenticate(any())).thenReturn(autenticacaoMock);
        when(tokenService.gerarToken(usuarioPadrao)).thenReturn("token-jwt-gerado");

        LoginRequestDTO request = new LoginRequestDTO("joao@email.com", "minhasenha");
        LoginResponseDTO resultado = authService.autenticarUsuario(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.token()).isEqualTo("token-jwt-gerado");
        assertThat(resultado.name()).isEqualTo("João Silva");
        assertThat(resultado.role()).isEqualTo(UsuarioRole.ROLE_CLIENTE.name());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando credenciais são inválidas")
    void deveLancarExcecaoQuandoCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        LoginRequestDTO request = new LoginRequestDTO("errado@email.com", "senhaerrada");

        assertThatThrownBy(() -> authService.autenticarUsuario(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email ou senha inválidos");
    }

    @Test
    @DisplayName("Deve registrar novo usuário cliente com sucesso")
    void deveRegistrarClienteComSucesso() {
        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCodificada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId("novo-id");
            return u;
        });

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Maria Santos", "novo@email.com", "senha123", "11999998888");

        RegisterResponseDTO resultado = authService.registrarUsuario(request, UsuarioRole.ROLE_CLIENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo("novo@email.com");
        assertThat(resultado.role()).isEqualTo(UsuarioRole.ROLE_CLIENTE.name());
        assertThat(resultado.message()).contains("sucesso");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando email já está cadastrado no registro")
    void deveLancarExcecaoQuandoEmailJaCadastradoNoRegistro() {
        when(usuarioRepository.existsByEmail("existente@email.com")).thenReturn(true);

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Fulano", "existente@email.com", "senha123", "11999998888");

        assertThatThrownBy(() -> authService.registrarUsuario(request, UsuarioRole.ROLE_CLIENTE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    @Test
    @DisplayName("Deve registrar proprietário de barbearia com role correta")
    void deveRegistrarProprietarioDeBarbeariaComRoleCorreta() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCodificada");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Carlos Dono", "carlos@barbearia.com", "senha123", "11988887777");

        RegisterResponseDTO resultado = authService.registrarUsuario(request, UsuarioRole.ROLE_BARBEARIA_ADM);

        assertThat(resultado.role()).isEqualTo(UsuarioRole.ROLE_BARBEARIA_ADM.name());
    }

    @Test
    @DisplayName("Deve renovar token JWT com sucesso quando token é válido")
    void deveRenovarTokenComSucesso() {
        when(tokenService.validarToken("token-valido")).thenReturn("joao@email.com");
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuarioPadrao));
        when(tokenService.gerarToken(usuarioPadrao)).thenReturn("novo-token-jwt");

        LoginResponseDTO resultado = authService.renovarToken("token-valido");

        assertThat(resultado.token()).isEqualTo("novo-token-jwt");
        assertThat(resultado.message()).contains("renovado");
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando token para renovação é inválido ou expirado")
    void deveLancarExcecaoQuandoTokenParaRenovacaoEhInvalido() {
        when(tokenService.validarToken("token-invalido")).thenReturn(null);

        assertThatThrownBy(() -> authService.renovarToken("token-invalido"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token inválido ou expirado");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário do token não é encontrado")
    void deveLancarExcecaoQuandoUsuarioDoTokenNaoExiste() {
        when(tokenService.validarToken("token-usuario-deletado")).thenReturn("deletado@email.com");
        when(usuarioRepository.findByEmail("deletado@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.renovarToken("token-usuario-deletado"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }
}
