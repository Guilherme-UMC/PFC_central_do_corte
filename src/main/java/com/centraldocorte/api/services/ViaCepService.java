package com.centraldocorte.api.services;

import com.centraldocorte.api.dto.ViaCepResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ViaCepService {

    private final RestTemplate restTemplate;

    private static final String URL_VIA_CEP = "https://viacep.com.br/ws/{cep}/json/";
    private static final int TAMANHO_CEP_VALIDO = 8;

    public ViaCepResponseDTO buscarEnderecoPorCep(String cep) {
        String cepSemFormatacao = removerCaracteresNaoNumericos(cep);
        validarTamanhoDoCep(cepSemFormatacao);

        try {
            ViaCepResponseDTO resposta = restTemplate.getForObject(URL_VIA_CEP, ViaCepResponseDTO.class, cepSemFormatacao);
            validarRespostaDaApiViaCep(resposta);
            return resposta;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erro ao consultar CEP na API ViaCep: " + e.getMessage());
        }
    }
    private String removerCaracteresNaoNumericos(String cep) {
        return cep.replaceAll("\\D", "");
    }

    private void validarTamanhoDoCep(String cep) {
        if (cep.length() != TAMANHO_CEP_VALIDO) {
            throw new BusinessException("CEP deve conter exatamente 8 dígitos numéricos");
        }
    }

    private void validarRespostaDaApiViaCep(ViaCepResponseDTO resposta) {
        if (resposta == null || resposta.erro()) {
            throw new BusinessException("CEP não encontrado");
        }
    }
}
