package com.centraldocorte.api.domain.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CategoriaServico {
    CORTE("Corte de Cabelo"),
    BARBA("Barba"),
    CABELO_E_BARBA("Cabelo e Barba"),
    QUIMICA("Química (Progressiva, Relaxamento)"),
    TINTURA("Tintura e Coloração"),
    SOBRANCELHA("Sobrancelha"),
    OUTROS("Outros Serviços");

    private final String descricao;

    CategoriaServico(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static CategoriaServico fromString(String value) {
        if (value == null) return OUTROS;

        for (CategoriaServico categoria : CategoriaServico.values()) {
            if (categoria.name().equalsIgnoreCase(value) ||
                    categoria.getDescricao().equalsIgnoreCase(value)) {
                return categoria;
            }
        }
        return OUTROS;
    }

    public static List<String> getNomes() {
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static List<String> getDescricoes() {
        return Arrays.stream(values())
                .map(CategoriaServico::getDescricao)
                .collect(Collectors.toList());
    }
}