package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Produto;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ProdutoRepository;
import com.centraldocorte.api.dto.ProdutoRequestDTO;
import com.centraldocorte.api.dto.ProdutoResponseDTO;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public ProdutoResponseDTO criarProduto(String barbeariaId, ProdutoRequestDTO request) {
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada"));

        Produto produto = Produto.builder()
                .barbearia(barbearia)
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .preco(request.getPreco())
                .imagemUrl(request.getImagemUrl())
                .categoria(request.getCategoria())
                .marca(request.getMarca())
                .ativo(true)
                .build();

        Produto saved = produtoRepository.save(produto);
        log.info("Produto criado: {} para barbearia {}", saved.getNome(), barbearia.getNome());

        return converterParaResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarProdutosPorBarbearia(String barbeariaId, Pageable pageable) {
        return produtoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarTodosProdutosAtivosPorBarbearia(String barbeariaId) {
        return produtoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listarCategoriasPorBarbearia(String barbeariaId) {
        return produtoRepository.findCategoriasByBarbeariaId(barbeariaId);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarProdutosPorCategoria(String barbeariaId, String categoria) {
        return produtoRepository.findByBarbeariaIdAndAtivoTrueAndCategoria(barbeariaId, categoria)
                .stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoResponseDTO atualizarProduto(String produtoId, ProdutoRequestDTO request) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        produto.setNome(request.getNome());
        produto.setDescricao(request.getDescricao());
        produto.setPreco(request.getPreco());
        produto.setImagemUrl(request.getImagemUrl());
        produto.setCategoria(request.getCategoria());
        produto.setMarca(request.getMarca());

        Produto updated = produtoRepository.save(produto);
        log.info("Produto atualizado: {}", updated.getNome());

        return converterParaResponseDTO(updated);
    }

    @Transactional
    public void desativarProduto(String produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        produto.setAtivo(false);
        produtoRepository.save(produto);
        log.info("Produto desativado: {}", produto.getNome());
    }

    @Transactional
    public void ativarProduto(String produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        produto.setAtivo(true);
        produtoRepository.save(produto);
        log.info("Produto ativado: {}", produto.getNome());
    }

    private ProdutoResponseDTO converterParaResponseDTO(Produto produto) {
        return ProdutoResponseDTO.builder()
            .id(produto.getId())
            .barbeariaId(produto.getBarbearia().getId())
            .barbeariaNome(produto.getBarbearia().getNome())
            .nome(produto.getNome())
            .descricao(produto.getDescricao())
            .preco(produto.getPreco())
            .imagemUrl(produto.getImagemUrl())
            .categoria(produto.getCategoria())
            .marca(produto.getMarca())
            .ativo(produto.getAtivo())
            .criadoEm(produto.getCriadoEm())
            .atualizadoEm(produto.getAtualizadoEm())
            .build();
    }
}