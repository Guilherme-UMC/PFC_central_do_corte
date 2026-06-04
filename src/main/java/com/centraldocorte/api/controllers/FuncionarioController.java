package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.*;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.services.FuncionarioService;
import com.centraldocorte.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Funcionários", description = "Endpoints para gerenciamento de funcionários")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final UsuarioService usuarioService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Criar novo funcionário")
    public ResponseEntity<UsuarioResponseDTO> criarFuncionario(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId,
            @Valid @RequestBody RegisterRequestDTO request) {

        UsuarioResponseDTO response = funcionarioService.criarFuncionario(barbeariaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/barbearia/{barbeariaId}/vincular")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Vincular funcionário existente")
    public ResponseEntity<Void> vincularFuncionarioExistente(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId,
            @Valid @RequestBody FuncionarioVinculoDTO dto) {

        funcionarioService.vincularFuncionarioExistente(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Listar todos os funcionários vinculados a uma barbearia")
    public ResponseEntity<List<UsuarioResponseDTO>> listarFuncionariosPorBarbearia(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId) {

        return ResponseEntity.ok(funcionarioService.listarFuncionariosPorBarbearia(barbeariaId));
    }

    @GetMapping("/barbearia/{barbeariaId}/disponiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'CLIENTE')")
    @Operation(summary = "Listar funcionários disponíveis para agendamento")
    public ResponseEntity<List<UsuarioResponseDTO>> listarFuncionariosDisponiveisParaAgendamento(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId) {

        return ResponseEntity.ok(funcionarioService.listarFuncionariosDisponiveisParaAgendamento(barbeariaId));
    }

    @GetMapping("/disponiveis-para-contratacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Listar funcionários disponíveis para contratação (sem vínculo)")
    public ResponseEntity<List<UsuarioResponseDTO>> listarFuncionariosDisponiveisParaContratacao() {
        return ResponseEntity.ok(funcionarioService.listarFuncionariosDisponiveisParaContratacao());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Listar todos os funcionários do sistema")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodosFuncionarios() {
        return ResponseEntity.ok(usuarioService.listarTodosFuncionarios());
    }

    @GetMapping("/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Buscar funcionário por ID")
    public ResponseEntity<UsuarioResponseDTO> buscarFuncionarioPorId(@PathVariable String funcionarioId) {
        return ResponseEntity.ok(usuarioService.buscarPorId(funcionarioId));
    }

    @PutMapping("/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Atualizar dados do funcionário")
    public ResponseEntity<UsuarioResponseDTO> atualizarFuncionario(
            @PathVariable String funcionarioId,
            @Valid @RequestBody UsuarioUpdateDTO request) {

        return ResponseEntity.ok(usuarioService.atualizarUsuario(funcionarioId, request));
    }

    @DeleteMapping("/barbearia/{barbeariaId}/desvincular/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Desvincular funcionário (mantém conta ativa)")
    public ResponseEntity<?> desvincularFuncionario(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId,
            @Parameter(description = "ID do funcionário", required = true)
            @PathVariable String funcionarioId) {

        try {
            funcionarioService.desvincularFuncionario(barbeariaId, funcionarioId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Funcionário desvinculado com sucesso");
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "ERROR");
            errorResponse.put("suggestion", "Remaneje os agendamentos manualmente antes de desvincular o funcionário");

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }
    }

    @PatchMapping("/barbearia/{barbeariaId}/{funcionarioId}/disponibilidade")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Alternar disponibilidade temporária (férias/folgas)")
    public ResponseEntity<Void> alternarDisponibilidadeFuncionario(
            @Parameter(description = "ID da barbearia", required = true)
            @PathVariable String barbeariaId,
            @Parameter(description = "ID do funcionário", required = true)
            @PathVariable String funcionarioId) {

        funcionarioService.alternarDisponibilidadeFuncionario(barbeariaId, funcionarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/barbearia/{barbeariaId}/disponibilidade")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'CLIENTE')")
    @Operation(summary = "Verificar se funcionário está disponível em um horário")
    public ResponseEntity<Boolean> verificarDisponibilidadeDoFuncionario(
            @PathVariable String barbeariaId,
            @RequestParam String funcionarioId,
            @RequestParam String dataHora) {

        boolean disponivel = funcionarioService.verificarDisponibilidadeDoFuncionario(
                barbeariaId, funcionarioId, LocalDateTime.parse(dataHora));

        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/barbearias")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @Operation(summary = "Listar barbearias onde o funcionário está vinculado")
    public ResponseEntity<List<BarbeariaResponseDTO>> listarBarbeariasDoFuncionario(Authentication authentication) {
        String email = authentication.getName();

        Usuario funcionario = usuarioService.buscarPorEmail(email);

        List<BarbeariaResponseDTO> barbearias = funcionarioService.listarBarbeariasPorFuncionario(funcionario.getId());
        return ResponseEntity.ok(barbearias);
    }
}