package com.example.crud.domain.user;

public record RequestUser(
        String name,
        String email,
        String password,
        String telefone,
        UserRole role
) {}