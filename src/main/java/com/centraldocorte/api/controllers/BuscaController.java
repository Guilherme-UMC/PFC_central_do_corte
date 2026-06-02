package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.BuscaResponseDTO;
import com.centraldocorte.api.services.BuscaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/busca")
@RequiredArgsConstructor
@Tag(name = "Busca Global", description = "Endpoint unificado para busca no sistema")
public class BuscaController {

    private final BuscaService buscaService;

    @GetMapping
    @Operation(summary = "Busca global",
            description = "Busca por barbearias, serviços e funcionários em um único endpoint")
    public ResponseEntity<BuscaResponseDTO> buscarGlobal(
            @Parameter(description = "Termo de busca", required = true, example = "corte")
            @RequestParam String q) {

        BuscaResponseDTO resultado = buscaService.buscarGlobal(q);
        return ResponseEntity.ok(resultado);
    }
}