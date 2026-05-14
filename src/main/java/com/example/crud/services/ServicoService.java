package com.example.crud.services;

import com.example.crud.domain.models.Barbearia;
import com.example.crud.domain.models.Servico;
import com.example.crud.domain.repositories.BarbeariaRepository;
import com.example.crud.domain.repositories.ServicoRepository;
import com.example.crud.dto.ServicoDTO;
import com.example.crud.dto.ServicoResponseDTO;
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
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada"));

        Servico servico = new Servico();
        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());
        servico.setBarbearia(barbearia);

        return servicoRepository.save(servico);
    }

    public List<Servico> listarServicosPorBarbearia(String barbeariaId) {
        return servicoRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId);
    }

    @Transactional
    public Servico atualizarServico(String servicoId, ServicoDTO dto) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setNome(dto.getNome());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());

        return servicoRepository.save(servico);
    }

    @Transactional
    public void desativarServico(String servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    // Adicione no ServicoService.java
    public ServicoResponseDTO convertToResponseDTO(Servico servico) {
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
}
