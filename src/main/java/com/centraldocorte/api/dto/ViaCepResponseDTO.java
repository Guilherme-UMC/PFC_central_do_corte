package com.centraldocorte.api.dto;

public record ViaCepResponseDTO (
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    String estado,
    String regiao,
    String ibge,
    String gia,
    String ddd,
    String siafi,
    boolean erro
){}
