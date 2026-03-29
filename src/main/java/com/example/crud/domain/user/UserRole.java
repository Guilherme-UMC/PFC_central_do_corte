package com.example.crud.domain.user;

public enum UserRole {
    ROLE_ADMIN("admin"),
    ROLE_CLIENTE("cliente"),
    ROLE_FUNCIONARIO("funcionario"),
    ROLE_BARBEARIA_ADM("barbeariaADM");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}