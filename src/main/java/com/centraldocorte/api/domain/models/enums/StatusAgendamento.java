package com.centraldocorte.api.domain.models.enums;

public enum StatusAgendamento {
    PENDENTE("Aguardando confirmação"),
    CONFIRMADO("Confirmado"),
    CANCELADO_PELO_CLIENTE("Cancelado pelo cliente"),
    CANCELADO_PELA_BARBEARIA("Cancelado pela barbearia"),
    CONCLUIDO("Concluído"),
    NAO_COMPARECEU("Não compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}