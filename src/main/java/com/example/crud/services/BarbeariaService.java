package com.example.crud.services;

import com.example.crud.domain.barbearia.Barbearia;
import com.example.crud.domain.barbearia.BarbeariaRepository;
import com.example.crud.domain.user.User;
import com.example.crud.dto.BarbeariaRequestDTO;
import com.example.crud.dto.BarbeariaResponseDTO;
import com.example.crud.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarbeariaService {

    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public BarbeariaResponseDTO criar(BarbeariaRequestDTO request, User owner) {
        Barbearia barbearia = mapToEntity(request);
        barbearia.setOwner(owner);
        barbearia = barbeariaRepository.save(barbearia);

        return mapToResponseDTO(barbearia);
    }

    @Transactional(readOnly = true)
    public Page<BarbeariaResponseDTO> listarTodos(Pageable pageable) {
        return barbeariaRepository.findAllByAtivoTrue(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public BarbeariaResponseDTO buscarPorId(String id) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        return mapToResponseDTO(barbearia);
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorOwner(User owner) {
        return barbeariaRepository.findByOwnerAndAtivoTrue(owner)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorOwnerId(String ownerId) {
        return barbeariaRepository.findByOwnerIdAndAtivoTrue(ownerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorNome(String nome) {
        return barbeariaRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorCidadeUf(String cidade, String uf) {
        return barbeariaRepository.findByCidadeAndUfAndAtivoTrue(cidade, uf)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BarbeariaResponseDTO atualizar(String id, BarbeariaRequestDTO request) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        updateEntityFromRequest(barbearia, request);
        barbearia = barbeariaRepository.save(barbearia);

        return mapToResponseDTO(barbearia);
    }

    @Transactional
    public void deletar(String id) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        barbearia.setAtivo(false);
        barbeariaRepository.save(barbearia);
    }

    @Transactional
    public void ativarDesativar(String id) {
        Barbearia barbearia = barbeariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        barbearia.setAtivo(!barbearia.getAtivo());
        barbeariaRepository.save(barbearia);
    }

    private Barbearia mapToEntity(BarbeariaRequestDTO request) {
        Barbearia barbearia = new Barbearia();
        barbearia.setNome(request.getNome());
        barbearia.setDescricao(request.getDescricao());
        barbearia.setLogradouro(request.getLogradouro());
        barbearia.setNumero(request.getNumero());
        barbearia.setBairro(request.getBairro());
        barbearia.setCep(request.getCep());
        barbearia.setCidade(request.getCidade());
        barbearia.setUf(request.getUf());
        barbearia.setImgUrl(request.getImgUrl());
        barbearia.setTelefone(request.getTelefone());
        barbearia.setAtivo(true);
        return barbearia;
    }

    private void updateEntityFromRequest(Barbearia barbearia, BarbeariaRequestDTO request) {
        barbearia.setNome(request.getNome());
        barbearia.setDescricao(request.getDescricao());
        barbearia.setLogradouro(request.getLogradouro());
        barbearia.setNumero(request.getNumero());
        barbearia.setBairro(request.getBairro());
        barbearia.setCep(request.getCep());
        barbearia.setCidade(request.getCidade());
        barbearia.setUf(request.getUf());
        barbearia.setImgUrl(request.getImgUrl());
        barbearia.setTelefone(request.getTelefone());
    }

    private BarbeariaResponseDTO mapToResponseDTO(Barbearia barbearia) {
        return BarbeariaResponseDTO.builder()
                .id(barbearia.getId())
                .ownerId(barbearia.getOwner() != null ? barbearia.getOwner().getId() : null)
                .ownerName(barbearia.getOwner() != null ? barbearia.getOwner().getName() : null)
                .nome(barbearia.getNome())
                .descricao(barbearia.getDescricao())
                .logradouro(barbearia.getLogradouro())
                .numero(barbearia.getNumero())
                .bairro(barbearia.getBairro())
                .cep(barbearia.getCep())
                .cidade(barbearia.getCidade())
                .uf(barbearia.getUf())
                .imgUrl(barbearia.getImgUrl())
                .telefone(barbearia.getTelefone())
                .criadoEm(barbearia.getCriadoEm())
                .atualizadoEm(barbearia.getAtualizadoEm())
                .ativo(barbearia.getAtivo())
                .build();
    }
}