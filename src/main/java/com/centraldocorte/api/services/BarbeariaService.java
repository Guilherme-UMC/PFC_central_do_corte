package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.dto.BarbeariaRequestDTO;
import com.centraldocorte.api.dto.BarbeariaResponseDTO;
import com.centraldocorte.api.dto.ViaCepResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BarbeariaService {

    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public BarbeariaResponseDTO criarBarbearia(BarbeariaRequestDTO request, Usuario proprietario) {
        Barbearia barbearia = montarBarbeariaAPartirDoRequest(request);
        barbearia.setOwner(proprietario);
        Barbearia barbeariaSalva = barbeariaRepository.save(barbearia);
        return converterParaResponseDTO(barbeariaSalva);
    }

    @Transactional(readOnly = true)
    public Page<BarbeariaResponseDTO> listarBarbeariasAtivas(Pageable pageable) {
        return barbeariaRepository.findAllByAtivoTrue(pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public BarbeariaResponseDTO buscarPorId(String id) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        return converterParaResponseDTO(barbearia);
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorProprietario(Usuario proprietario) {
        return barbeariaRepository.findByOwnerAndAtivoTrue(proprietario)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorIdProprietario(String proprietarioId) {
        return barbeariaRepository.findByOwnerIdAndAtivoTrue(proprietarioId)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorNome(String nome) {
        return barbeariaRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorCidadeUf(String cidade, String uf) {
        return barbeariaRepository.findByCidadeAndUfAndAtivoTrue(cidade, uf)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public BarbeariaResponseDTO atualizarBarbearia(String id, BarbeariaRequestDTO request) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        atualizarCamposBarbearia(barbearia, request);
        Barbearia barbeariaSalva = barbeariaRepository.save(barbearia);
        return converterParaResponseDTO(barbeariaSalva);
    }

    @Transactional
    public void desativarBarbearia(String id) {
        Barbearia barbearia = barbeariaRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        barbearia.setAtivo(false);
        barbeariaRepository.save(barbearia);
    }

    @Transactional
    public void alternarStatusBarbearia(String id) {
        Barbearia barbearia = barbeariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada com ID: " + id));

        barbearia.setAtivo(!barbearia.getAtivo());
        barbeariaRepository.save(barbearia);
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> buscarPorCep(String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");

        return barbeariaRepository.findByCepAndAtivoTrue(cepLimpo)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    public Map<String, Object> buscarBarbeariasPorCepComFallback(String cep, ViaCepService viaCepService) {
        String cepLimpo = cep.replaceAll("\\D", "");
        List<BarbeariaResponseDTO> barbeariasPorCep = buscarPorCep(cepLimpo);

        Map<String, Object> resposta = new LinkedHashMap<>();

        if (!barbeariasPorCep.isEmpty()) {
            resposta.put("tipo_busca", "CEP_EXATO");
            resposta.put("total", barbeariasPorCep.size());
            resposta.put("barbearias", barbeariasPorCep);
            return resposta;
        }

        try {
            ViaCepResponseDTO endereco = viaCepService.buscarEnderecoPorCep(cepLimpo);
            List<BarbeariaResponseDTO> barbeariasPorCidade = buscarPorCidadeUf(
                    endereco.localidade(),
                    endereco.uf()
            );

            resposta.put("cep_consultado", cep);
            resposta.put("endereco_do_cep", Map.of(
                    "cidade", endereco.localidade(),
                    "uf", endereco.uf(),
                    "logradouro", endereco.logradouro(),
                    "bairro", endereco.bairro()
            ));

            if (!barbeariasPorCidade.isEmpty()) {
                resposta.put("tipo_busca", "CIDADE_PROXIMA");
                resposta.put("total", barbeariasPorCidade.size());
                resposta.put("barbearias", barbeariasPorCidade);
                resposta.put("mensagem", "Nenhuma barbearia encontrada no CEP exato, mas encontramos na mesma cidade");
            } else {
                resposta.put("tipo_busca", "SEM_RESULTADOS");
                resposta.put("mensagem", "Nenhuma barbearia encontrada neste CEP ou na cidade correspondente");
            }

            return resposta;

        } catch (BusinessException e) {
            resposta.put("erro", "CEP_INVALIDO");
            resposta.put("mensagem", e.getMessage());
            return resposta;
        }
    }

    private Barbearia montarBarbeariaAPartirDoRequest(BarbeariaRequestDTO request) {
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

    private void atualizarCamposBarbearia(Barbearia barbearia, BarbeariaRequestDTO request) {
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

    private BarbeariaResponseDTO converterParaResponseDTO(Barbearia barbearia) {
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