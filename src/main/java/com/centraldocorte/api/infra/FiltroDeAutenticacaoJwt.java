package com.centraldocorte.api.infra;

import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FiltroDeAutenticacaoJwt extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/",
            "/barbearia/buscar-por-cep/",
            "/barbearia/buscar-cep/",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs"
    );

    private static final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/barbearia"
    );

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicPath(uri, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extrairTokenDoHeader(request);

        if (token != null) {
            try {
                autenticarUsuarioComToken(token);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri, String method) {
        for (String path : PUBLIC_PATHS) {
            if (uri.startsWith(path)) {
                return true;
            }
        }

        if (method.equals("GET")) {
            for (String path : PUBLIC_GET_PATHS) {
                if (uri.equals(path) || uri.matches(path + "(\\?.*)?")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void autenticarUsuarioComToken(String token) {
        String email = tokenService.validarToken(token);
        boolean contextoSemAutenticacao = SecurityContextHolder.getContext().getAuthentication() == null;

        if (email != null && contextoSemAutenticacao) {
            usuarioRepository.findByEmailIgnoreCase(email).ifPresent(this::registrarAutenticacaoNoContexto);
        }
    }

    private void registrarAutenticacaoNoContexto(UserDetails usuario) {
        var autenticacao = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }

    private String extrairTokenDoHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(PREFIXO_BEARER)) {
            return null;
        }

        return authHeader.substring(PREFIXO_BEARER.length());
    }
}