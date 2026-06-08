package com.centraldocorte.api.infra;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - FiltroDeAutenticacaoJwt")
class FiltroDeAutenticacaoJwtTest {

    @Mock private TokenService tokenService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @InjectMocks private FiltroDeAutenticacaoJwt filtro;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir acesso a rotas públicas sem autenticação")
    void devePermitirAcessoRotasPublicas() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).validarToken(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve permitir acesso ao Swagger UI")
    void devePermitirAcessoSwaggerUi() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        when(request.getMethod()).thenReturn("GET");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir acesso ao OpenAPI docs")
    void devePermitirAcessoOpenApiDocs() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        when(request.getMethod()).thenReturn("GET");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir GET em /barbearia sem autenticação")
    void devePermitirGetBarbearia() throws Exception {
        when(request.getRequestURI()).thenReturn("/barbearia");
        when(request.getMethod()).thenReturn("GET");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("NÃO deve permitir POST em /barbearia sem autenticação")
    void naoDevePermitirPostBarbeariaSemAutenticacao() throws Exception {
        when(request.getRequestURI()).thenReturn("/barbearia");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve extrair token do header Authorization corretamente")
    void deveExtrairTokenDoHeaderAuthorization() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-jwt-valido-123");
        when(tokenService.validarToken("token-jwt-valido-123")).thenReturn("usuario@teste.com");

        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@teste.com");
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar null quando header Authorization não tem Bearer")
    void deveRetornarNullQuandoHeaderSemBearer() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Basic token-invalido");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).validarToken(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve retornar null quando header Authorization está vazio")
    void deveRetornarNullQuandoHeaderVazio() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve autenticar usuário quando token é válido")
    void deveAutenticarUsuarioQuandoTokenValido() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(tokenService.validarToken("token-valido")).thenReturn("usuario@teste.com");

        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@teste.com");
        usuario.setRole(UsuarioRole.ROLE_CLIENTE);
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(usuario);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("Não deve autenticar quando token é inválido")
    void naoDeveAutenticarQuandoTokenInvalido() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(tokenService.validarToken("token-invalido")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Não deve autenticar quando usuário não existe no banco")
    void naoDeveAutenticarQuandoUsuarioNaoExiste() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(tokenService.validarToken("token-valido")).thenReturn("naoexiste@teste.com");
        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve limpar contexto quando token lança exceção")
    void deveLimparContextoQuandoTokenLancaExcecao() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-exception");
        when(tokenService.validarToken("token-exception")).thenThrow(new RuntimeException("Token expirado"));

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve permitir acesso a /auth/register")
    void devePermitirAcessoAuthRegister() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/register");
        when(request.getMethod()).thenReturn("POST");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir acesso a /auth/refresh-token")
    void devePermitirAcessoAuthRefreshToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/refresh-token");
        when(request.getMethod()).thenReturn("POST");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir acesso a /barbearia/buscar-cep/{cep}")
    void devePermitirAcessoBuscarCep() throws Exception {
        when(request.getRequestURI()).thenReturn("/barbearia/buscar-cep/01001000");
        when(request.getMethod()).thenReturn("GET");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve permitir acesso a /api/servicos/barbearia/{barbeariaId}")
    void devePermitirAcessoServicosPublicos() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/servicos/barbearia/barb-123");
        when(request.getMethod()).thenReturn("GET");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve processar requisições OPTIONS (preflight CORS)")
    void deveProcessarRequisicoesOptions() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("OPTIONS");

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filtro deve processar rapidamente (performance)")
    void filtroDeveProcessarRapidamente() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/agendamentos");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(tokenService.validarToken("token-valido")).thenReturn("usuario@teste.com");

        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@teste.com");
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));

        long startTime = System.nanoTime();
        filtro.doFilterInternal(request, response, filterChain);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        assertThat(durationMs).isLessThan(100); // deve processar em menos de 100ms
        verify(filterChain).doFilter(request, response);
    }
}