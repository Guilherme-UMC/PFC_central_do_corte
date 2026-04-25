package com.example.crud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarbeariaResponseDTO {
    private String id;
    private String ownerId;
    private String ownerName;
    private String nome;
    private String descricao;
    private String logradouro;
    private String numero;
    private String bairro;
    private String cep;
    private String cidade;
    private String uf;
    private String imgUrl;
    private String telefone;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private Boolean ativo;
}