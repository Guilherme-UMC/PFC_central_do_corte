package com.example.crud.dto;

public record LoginResponseDTO (
        String token,
        String userId,
        String name,
        String role,
        String message
)
{}
