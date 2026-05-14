package com.example.crud.controllers;

import com.example.crud.domain.models.User;
import com.example.crud.domain.models.UserRole;
import com.example.crud.dto.FuncionarioVinculoDTO;
import com.example.crud.dto.UserRequestDTO;
import com.example.crud.dto.UserResponseDTO;
import com.example.crud.services.FuncionarioService;
import com.example.crud.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-auth")
@Tag(name = "Funcionários", description = "Endpoints para gerenciamento de funcionários")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final UsuarioService usuarioService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Criar novo funcionário e vincular à barbearia")
    public ResponseEntity<UserResponseDTO> criarFuncionario(
            @PathVariable String barbeariaId,
            @Valid @RequestBody UserRequestDTO request) {

        User novoFuncionario = new User();
        novoFuncionario.setName(request.getName());
        novoFuncionario.setEmail(request.getEmail());
        novoFuncionario.setPassword(request.getPassword());
        novoFuncionario.setTelefone(request.getTelefone());
        novoFuncionario.setRole(UserRole.ROLE_FUNCIONARIO);
        novoFuncionario.setActive(true);

        User saved = funcionarioService.criarFuncionario(barbeariaId, novoFuncionario);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDTO(saved));
    }

    @PostMapping("/barbearia/{barbeariaId}/vincular")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEARIA_ADM')")
    @Operation(summary = "Vincular funcionário existente à barbearia")
    public ResponseEntity<Void> vincularFuncionarioExistente(
            @PathVariable String barbeariaId,
            @Valid @RequestBody FuncionarioVinculoDTO dto) {

        funcionarioService.vincularFuncionarioExistente(barbeariaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    @Operation(summary = "Listar todos os funcionários de uma barbearia")
    public ResponseEntity<List<UserResponseDTO>> listarFuncionariosPorBarbearia(
            @PathVariable String barbeariaId) {

        List<UserResponseDTO> funcionarios = funcionarioService.listarFuncionariosPorBarbearia(barbeariaId)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();

        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Listar funcionários disponíveis (não vinculados a nenhuma barbearia)")
    public ResponseEntity<List<UserResponseDTO>> listarFuncionariosDisponiveis() {
        List<UserResponseDTO> funcionarios = usuarioService.findFuncionariosDisponiveis();
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os funcionários do sistema (apenas ADMIN)")
    public ResponseEntity<List<UserResponseDTO>> listarTodosFuncionarios() {
        List<UserResponseDTO> funcionarios = usuarioService.findAllFuncionarios();
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM', 'FUNCIONARIO')")
    @Operation(summary = "Buscar funcionário por ID")
    public ResponseEntity<UserResponseDTO> buscarFuncionarioPorId(
            @PathVariable String funcionarioId) {

        UserResponseDTO funcionario = usuarioService.findById(funcionarioId);
        return ResponseEntity.ok(funcionario);
    }

    @PutMapping("/{funcionarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @Operation(summary = "Atualizar dados do funcionário")
    public ResponseEntity<UserResponseDTO> atualizarFuncionario(
            @PathVariable String funcionarioId,
            @Valid @RequestBody UserRequestDTO request) {

        UserResponseDTO updated = usuarioService.update(funcionarioId, request);
        return ResponseEntity.ok(updated);
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
    public ResponseEntity<Void> desativarFuncionario(
            @PathVariable String funcionarioId) {

        usuarioService.delete(funcionarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{funcionarioId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar/desativar funcionário (apenas ADMIN)")
    public ResponseEntity<Void> toggleFuncionarioStatus(
            @PathVariable String funcionarioId) {

        usuarioService.toggleStatus(funcionarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/barbearia/{barbeariaId}/disponibilidade")
    @PreAuthorize("hasAnyRole('BARBEARIA_ADM', 'CLIENTE')")
    @Operation(summary = "Verificar se funcionário está disponível em um horário")
    public ResponseEntity<Boolean> verificarDisponibilidadeFuncionario(
            @PathVariable String barbeariaId,
            @RequestParam String funcionarioId,
            @RequestParam String dataHora) {

        boolean disponivel = funcionarioService.verificarDisponibilidadeFuncionario(
                barbeariaId, funcionarioId, java.time.LocalDateTime.parse(dataHora));

        return ResponseEntity.ok(disponivel);
    }

    /**
     * Converte User para UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .telefone(user.getTelefone())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}