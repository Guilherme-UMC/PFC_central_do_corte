package com.example.crud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 3, max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @Pattern(regexp = "\\d{2}\\s?\\d{4,5}?\\d{4}") String telefone
) {}
