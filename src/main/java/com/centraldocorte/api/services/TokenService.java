package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class TokenService {

    private static final long DURACAO_ACCESS_TOKEN_EM_MS = 1000L * 60 * 30;
    private static final long DURACAO_REFRESH_TOKEN_EM_MS = 1000L * 60 * 60 * 24 * 7;

    @Value("${api.security.token.secret}")
    private String segredo;

    public String gerarAccessToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("role", usuario.getRole().name());
        claims.put("type", "access");
        return criarToken(claims, usuario.getEmail(), DURACAO_ACCESS_TOKEN_EM_MS);
    }

    public String gerarRefreshToken(Usuario usuario){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("type", "refresh");
        return criarToken(claims, usuario.getEmail(), DURACAO_REFRESH_TOKEN_EM_MS);
    }

    private String criarToken(Map<String, Object> claims, String subject, long duracao) {
        Date agora = new Date(System.currentTimeMillis());
        Date expiracao = new Date(System.currentTimeMillis() + duracao);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(agora)
            .setExpiration(expiracao)
            .signWith(obterChaveDeAssinatura(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String validarAccessToken(String token) {
        try {
            Claims claims = extrairTodosClaims(token);
            String tipo = claims.get("type", String.class);

            if (!"access".equals(tipo)) {
                return null;
            }

            String email = claims.getSubject();
            if (tokenEstaExpirado(token)) {
                return null;
            }
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    public String validarRefreshToken(String token) {
        try {
            Claims claims = extrairTodosClaims(token);
            String tipo = claims.get("type", String.class);

            if (!"refresh".equals(tipo)) {
                return null;
            }

            String email = claims.getSubject();
            if (tokenEstaExpirado(token)) {
                return null;
            }
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    public String extrairUsuarioId(String token) {
        return extrairClaim(token, claims -> claims.get("userId", String.class));
    }

    public String validarToken(String token) {
        try {
            String email = extrairEmail(token);
            if (tokenEstaExpirado(token)) {
                return null;
            }
            return email;
        } catch (Exception e) {
            return null;
        }
    }

    public String extrairEmail(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public String extrairRole(String token) {
        return extrairClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extrairClaim(String token, Function<Claims, T> resolverDeClaim) {
        Claims claims = extrairTodosClaims(token);
        return resolverDeClaim.apply(claims);
    }

    private Key obterChaveDeAssinatura() {
        return Keys.hmacShaKeyFor(segredo.getBytes());
    }

    private Claims extrairTodosClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(obterChaveDeAssinatura())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private boolean tokenEstaExpirado(String token) {
        return extrairClaim(token, Claims::getExpiration).before(new Date());
    }
}