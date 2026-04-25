package com.example.crud.controllers;

import com.example.crud.domain.user.UserRole;
import com.example.crud.dto.*;
import com.example.crud.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Email ou senha inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registro de cliente", description = "Cria uma nova conta de cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerClient(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request, UserRole.ROLE_CLIENTE));
    }

    @Operation(summary = "Registro de dono de barbearia",
            description = "Cria um novo usuario proprietário de barbearia (ROLE_BARBEARIA_ADM)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Email já cadastrado ou dados inválidos")
    })
    @PostMapping("/register/barbearia")
    public ResponseEntity<RegisterResponseDTO> registerBarbeariaAdm(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request, UserRole.ROLE_BARBEARIA_ADM));
    }

    @Operation(summary = "Renovar token", description = "Gera um novo token JWT usando um token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer "
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}