package com.example.crud.dto;

public record RegisterResponseDTO(
        String userId,
        String name,
        String email,
        String role,
        String message
) {}