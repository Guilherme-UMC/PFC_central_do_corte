package com.centraldocorte.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequestDTO (
    @NotBlank String token,
    @NotBlank @Size(min=6) String novaSenha)
{}
