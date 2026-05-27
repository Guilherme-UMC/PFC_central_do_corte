package com.centraldocorte.api.domain.models.enums;

public enum UsuarioRole {
    ROLE_ADMIN("admin"),
    ROLE_CLIENTE("cliente"),
    ROLE_FUNCIONARIO("funcionario"),
    ROLE_BARBEARIA_ADM("barbeariaADM");

    private String role;

    UsuarioRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}