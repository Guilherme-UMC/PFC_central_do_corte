package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ServicoRepository;
import com.centraldocorte.api.dto.ServicoDTO;
import com.centraldocorte.api.dto.ServicoResponseDTO;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {
    private final ServicoRepository servicoRepository;
    private final BarbeariaRepository barbeariaRepository;

    @Transactional
    public Servico criarServico(String barbeariaId, ServicoDTO dto) {
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));

        Servico servico = montarServicoAPartirDoRequest(dto, barbearia);
        return servicoRepository.save(servico);
    }

    @Transactional(readOnly = true)
    public List<Servico> listarServicosAtivosBarbearia(String barbeariaId) {
        return servicoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId);
    }

    @Transactional
    public Servico atualizarServico(String servicoId, ServicoDTO dto) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + servicoId));

        atualizarCamposDoServico(servico, dto);
        return servicoRepository.save(servico);
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
        return servico;
    }

    private void atualizarCamposDoServico(Servico servico, ServicoDTO dto) {
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());
    }
}