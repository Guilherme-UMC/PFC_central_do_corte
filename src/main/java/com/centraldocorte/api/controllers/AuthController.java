package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.LoginRequestDTO;
import com.centraldocorte.api.dto.LoginResponseDTO;
import com.centraldocorte.api.dto.RegisterRequestDTO;
import com.centraldocorte.api.dto.RegisterResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {
    private static final int TAMANHO_PREFIXO_BEARER = 7;

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Email ou senha inválidos")
    })
    public ResponseEntity<LoginResponseDTO> autenticarUsuario(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.autenticarUsuario(request, httpRequest));
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de cliente", description = "Cria uma nova conta de cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Email já cadastrado ou dados inválidos")
    })
    public ResponseEntity<RegisterResponseDTO> registrarCliente(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.registrarUsuario(request, UsuarioRole.ROLE_CLIENTE, httpRequest));
    }

    @PostMapping("/register/barbearia")
    @Operation(summary = "Registro de proprietário de barbearia",
            description = "Cria um novo usuário proprietário de barbearia (ROLE_BARBEARIA_ADM)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Email já cadastrado ou dados inválidos")
    })
    public ResponseEntity<RegisterResponseDTO> registrarProprietarioDeBarbearia(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.registrarUsuario(request, UsuarioRole.ROLE_BARBEARIA_ADM, httpRequest));
    }

    @PostMapping("/refresh-token")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Renovar token", description = "Gera um novo token JWT usando um token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<LoginResponseDTO> renovarToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(TAMANHO_PREFIXO_BEARER);
        return ResponseEntity.ok(authService.renovarToken(token));
    }


    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Registra o logout do usuário")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {

        authService.registrarLogout(httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirmar-email")
    @Operation(summary = "Confirmar e-mail", description = "Ativa a conta do usuário após confirmação por e-mail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();

        try {
            authService.confirmarEmail(token);
            response.put("message", "E-mail confirmado com sucesso! Você já pode fazer login.");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("message", e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reenviar-confirmacao")
    @Operation(summary = "Reenviar e-mail de confirmação")
    public ResponseEntity<Map<String, String>> reenviarConfirmacao(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();

        try {
            authService.reenviarEmailConfirmacao(email);
            response.put("message", "E-mail de confirmação reenviado! Verifique sua caixa de entrada.");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            response.put("message", e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }
}