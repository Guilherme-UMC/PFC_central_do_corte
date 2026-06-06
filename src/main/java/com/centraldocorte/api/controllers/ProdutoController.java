package com.centraldocorte.api.controllers;

import com.centraldocorte.api.dto.ProdutoRequestDTO;
import com.centraldocorte.api.dto.ProdutoResponseDTO;
import com.centraldocorte.api.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Gerenciamento de produtos das barbearias")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Criar novo produto")
    public ResponseEntity<ProdutoResponseDTO> criarProduto(
            @PathVariable String barbeariaId,
            @Valid @RequestBody ProdutoRequestDTO request) {

        ProdutoResponseDTO response = produtoService.criarProduto(barbeariaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/barbearia/{barbeariaId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar produtos ativos de uma barbearia (paginado)")
    public ResponseEntity<Page<ProdutoResponseDTO>> listarProdutosPorBarbearia(
            @PathVariable String barbeariaId,
            @PageableDefault(size = 12, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(produtoService.listarProdutosPorBarbearia(barbeariaId, pageable));
    }

    @GetMapping("/barbearia/{barbeariaId}/todos")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar todos os produtos ativos de uma barbearia")
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodosProdutosPorBarbearia(
            @PathVariable String barbeariaId) {

        return ResponseEntity.ok(produtoService.listarTodosProdutosAtivosPorBarbearia(barbeariaId));
    }

    @GetMapping("/barbearia/{barbeariaId}/categorias")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar categorias de produtos de uma barbearia")
    public ResponseEntity<List<String>> listarCategoriasPorBarbearia(
            @PathVariable String barbeariaId) {

        return ResponseEntity.ok(produtoService.listarCategoriasPorBarbearia(barbeariaId));
    }

    @GetMapping("/barbearia/{barbeariaId}/categoria/{categoria}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar produtos por categoria")
    public ResponseEntity<List<ProdutoResponseDTO>> listarProdutosPorCategoria(
            @PathVariable String barbeariaId,
            @PathVariable String categoria) {

        return ResponseEntity.ok(produtoService.listarProdutosPorCategoria(barbeariaId, categoria));
    }

    @PutMapping("/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @PathVariable String produtoId,
            @Valid @RequestBody ProdutoRequestDTO request) {

        return ResponseEntity.ok(produtoService.atualizarProduto(produtoId, request));
    }

    @DeleteMapping("/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativar produto")
    public ResponseEntity<Void> desativarProduto(@PathVariable String produtoId) {
        produtoService.desativarProduto(produtoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{produtoId}/ativar")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBEARIA_ADM')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ativar produto")
    public ResponseEntity<Void> ativarProduto(@PathVariable String produtoId) {
        produtoService.ativarProduto(produtoId);
        return ResponseEntity.ok().build();
    }
}