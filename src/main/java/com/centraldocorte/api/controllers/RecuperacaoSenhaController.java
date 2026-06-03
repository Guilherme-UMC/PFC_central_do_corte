package com.centraldocorte.api.controllers;
import com.centraldocorte.api.dto.EsqueciSenhaRequestDTO;
import com.centraldocorte.api.dto.RedefinirSenhaRequestDTO;
import com.centraldocorte.api.services.RecuperacaoSenhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Recuperação de Senha", description = "Endpoints para recuperação de senha")
public class RecuperacaoSenhaController {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/esqueci-senha")
    @Operation(summary = "Solicitar redefinição de senha")
    public ResponseEntity<Map<String, String>> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequestDTO request) {
        recuperacaoSenhaService.solicitarRedefinicaoSenha(request.email());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Se o email existir, você receberá as instruções para redefinir sua senha.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefinir senha com token")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        recuperacaoSenhaService.redefinirSenha(request.token(), request.novaSenha());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Senha redefinida com sucesso! Faça login com sua nova senha.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validar-token")
    @Operation(summary = "Validar token de recuperação")
    public ResponseEntity<Map<String, Boolean>> validarToken(@RequestParam String token) {
        boolean valido = recuperacaoSenhaService.validarToken(token);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valido", valido);
        return ResponseEntity.ok(response);
    }
}