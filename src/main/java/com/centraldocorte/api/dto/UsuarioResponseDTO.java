package com.centraldocorte.api.dto;

import com.centraldocorte.api.domain.models.UsuarioRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private String id;
    private String name;
    private String email;
    private String telefone;
    private UsuarioRole role;
    private boolean active;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}