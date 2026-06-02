package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Servico;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.ServicoRepository;
import com.centraldocorte.api.dto.BuscaItemDTO;
import com.centraldocorte.api.dto.BuscaResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuscaService {

    private static final int LIMITE_RESULTADOS = 10;

    private final BarbeariaRepository barbeariaRepository;
    private final ServicoRepository servicoRepository;

    public BuscaResponseDTO buscarGlobal(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return BuscaResponseDTO.builder()
                    .termo(termo)
                    .barbearias(List.of())
                    .servicos(List.of())
                    .totalResultados(0)
                    .temMaisResultados(false)
                    .build();
        }

        String termoLower = termo.toLowerCase().trim();

        List<Barbearia> barbearias = barbeariaRepository
                .findByNomeContainingIgnoreCaseAndAtivoTrue(termoLower)
                .stream()
                .limit(LIMITE_RESULTADOS)
                .toList();

        List<Servico> servicos = servicoRepository
                .findByAtivoTrueAndNomeContainingIgnoreCase(termoLower)
                .stream()
                .limit(LIMITE_RESULTADOS)
                .toList();

        int total = barbearias.size() + servicos.size();

        return BuscaResponseDTO.builder()
                .termo(termo)
                .barbearias(converterBarbeariasParaBuscaItem(barbearias))
                .servicos(converterServicosParaBuscaItem(servicos))
                .totalResultados(total)
                .temMaisResultados(total >= LIMITE_RESULTADOS * 2)
                .build();
    }

    private List<BuscaItemDTO> converterBarbeariasParaBuscaItem(List<Barbearia> barbearias) {
        return barbearias.stream()
                .map(b -> BuscaItemDTO.builder()
                        .id(b.getId())
                        .tipo("BARBEARIA")
                        .nome(b.getNome())
                        .descricao(b.getDescricao())
                        .imagemUrl(b.getImgUrl())
                        .subtitulo(String.format("%s, %s - %s", b.getCidade(), b.getUf(), b.getTelefone()))
                        .link("/barbearia/" + b.getId())
                        .build())
                .collect(Collectors.toList());
    }

    private List<BuscaItemDTO> converterServicosParaBuscaItem(List<Servico> servicos) {
        return servicos.stream()
                .map(s -> BuscaItemDTO.builder()
                        .id(s.getId())
                        .tipo("SERVICO")
                        .nome(s.getNome())
                        .descricao(s.getDescricao())
                        .subtitulo(String.format("R$ %.2f | %d min | %s",
                                s.getPreco(), s.getDuracaoMinutos(), s.getBarbearia().getNome()))
                        .link("/barbearia/" + s.getBarbearia().getId())
                        .build())
                .collect(Collectors.toList());
    }
}