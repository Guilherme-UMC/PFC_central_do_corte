package com.centraldocorte.api.dto;

public record LoginResponseDTO (
        String token,
        String userId,
        String name,
        String role,
        String message
)
{}