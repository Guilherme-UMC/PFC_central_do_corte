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

    private static final long DURACAO_TOKEN_EM_MS = 1000L * 60 * 60 * 2; // 2 hrs

    @Value("${api.security.token.secret}")
    private String segredo;

    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("role", usuario.getRole().name());
        return criarToken(claims, usuario.getEmail());
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

    public String extrairUsuarioId(String token) {
        return extrairClaim(token, claims -> claims.get("userId", String.class));
    }

    public <T> T extrairClaim(String token, Function<Claims, T> resolverDeClaim) {
        Claims claims = extrairTodosClaims(token);
        return resolverDeClaim.apply(claims);
    }

    private String criarToken(Map<String, Object> claims, String subject) {
        Date agora = new Date(System.currentTimeMillis());
        Date expiracao = new Date(System.currentTimeMillis() + DURACAO_TOKEN_EM_MS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(obterChaveDeAssinatura(), SignatureAlgorithm.HS256)
                .compact();
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