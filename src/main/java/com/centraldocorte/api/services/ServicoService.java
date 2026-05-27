package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ServicoRepository;
import com.centraldocorte.api.dto.ServicoDTO;  // ← ADICIONADO
import com.centraldocorte.api.dto.ServicoResponseDTO;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicoService {
    private final ServicoRepository servicoRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public ServicoResponseDTO criarServico(String barbeariaId, ServicoDTO dto) {  // ← CORRIGIDO
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));

        Servico servico = montarServicoAPartirDoRequest(dto, barbearia);
        Servico saved = servicoRepository.save(servico);
        return converterParaResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarServicosAtivosBarbearia(String barbeariaId) {
        return servicoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicoResponseDTO atualizarServico(String servicoId, ServicoDTO dto) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + servicoId));

        atualizarCamposDoServico(servico, dto);
        Servico updated = servicoRepository.save(servico);
        return converterParaResponseDTO(updated);
    }

    @Transactional
    public void desativarServico(String servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + servicoId));

        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    public ServicoResponseDTO converterParaResponseDTO(Servico servico) {
        ServicoResponseDTO dto = new ServicoResponseDTO();
        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());
        dto.setAtivo(servico.getAtivo());
        dto.setBarbeariaId(servico.getBarbearia().getId());
        dto.setBarbeariaNome(servico.getBarbearia().getNome());
        return dto;
    }

    private Servico montarServicoAPartirDoRequest(ServicoDTO dto, Barbearia barbearia) {
        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());
        servico.setBarbearia(barbearia);
        servico.setAtivo(true);
        return servico;
    }

    private void atualizarCamposDoServico(Servico servico, ServicoDTO dto) {
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());
    }
}