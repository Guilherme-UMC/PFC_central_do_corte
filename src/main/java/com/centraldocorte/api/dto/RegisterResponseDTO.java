package com.centraldocorte.api.dto;

public record RegisterResponseDTO(
        String userId,
        String name,
        String email,
        String role,
        String message
) {}