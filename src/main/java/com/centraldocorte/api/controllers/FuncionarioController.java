package com.centraldocorte.api.controllers;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.dto.FuncionarioVinculoDTO;
import com.centraldocorte.api.dto.UsuarioRequestDTO;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.services.FuncionarioService;
import com.centraldocorte.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Operation(summary = "Criar novo funcionário e vincular à barbearia")
    public ResponseEntity<UsuarioResponseDTO> criarFuncionario(
            @PathVariable String barbeariaId,
            @Valid @RequestBody UsuarioRequestDTO request) {

        Usuario novoFuncionario = montarFuncionarioAPartirDoRequest(request);
        Usuario funcionarioSalvo = funcionarioService.criarFuncionario(barbeariaId, novoFuncionario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.converterParaResponseDTO(funcionarioSalvo));
    }

    @PostMapping("/barbearia/{barbeariaId}/vincular")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Vincular funcionário existente à barbearia")
    public ResponseEntity<Void> vincularFuncionarioExistente(
            @PathVariable String barbeariaId,
            @Valid @RequestBody FuncionarioVinculoDTO dto) {

        funcionarioService.vincularFuncionarioExistente(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO', 'CLIENTE')")
    @Operation(summary = "Listar todos os funcionários de uma barbearia")
    public ResponseEntity<List<UsuarioResponseDTO>> listarFuncionariosPorBarbearia(
            @PathVariable String barbeariaId) {

        List<UsuarioResponseDTO> funcionarios = funcionarioService.listarFuncionariosBarbearia(barbeariaId)
                .stream()
                .map(usuarioService::converterParaResponseDTO)
                .toList();

        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Listar funcionários disponíveis (sem vínculo com nenhuma barbearia)")
    public ResponseEntity<List<UsuarioResponseDTO>> listarFuncionariosDisponiveis() {
        return ResponseEntity.ok(funcionarioService.listarFuncionariosDisponiveis());
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
            @Valid @RequestBody UsuarioRequestDTO request) {

        return ResponseEntity.ok(usuarioService.atualizarUsuario(funcionarioId, request));
    }

    @DeleteMapping("/barbearia/{barbeariaId}/desvincular/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Desvincular funcionário da barbearia")
    public ResponseEntity<Void> desvincularFuncionario(
            @PathVariable String barbeariaId,
            @PathVariable String funcionarioId) {

        funcionarioService.desvincularFuncionario(barbeariaId, funcionarioId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{funcionarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar funcionário (soft delete - apenas ADMIN)")
    public ResponseEntity<Void> desativarFuncionario(@PathVariable String funcionarioId) {
        usuarioService.desativarUsuario(funcionarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{funcionarioId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar/desativar funcionário (apenas ADMIN)")
    public ResponseEntity<Void> alternarStatusDoFuncionario(@PathVariable String funcionarioId) {
        usuarioService.alternarStatusDoUsuario(funcionarioId);
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

    private Usuario montarFuncionarioAPartirDoRequest(UsuarioRequestDTO request) {
        Usuario funcionario = new Usuario();
        funcionario.setName(request.getName());
        funcionario.setEmail(request.getEmail());
        funcionario.setPassword(request.getPassword());
        funcionario.setTelefone(request.getTelefone());
        funcionario.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        funcionario.setActive(true);
        return funcionario;
    }
}