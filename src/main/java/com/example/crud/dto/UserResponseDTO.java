package com.example.crud.dto;

import com.example.crud.domain.models.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String name;
    private String email;
    private String telefone;
    private UserRole role;
    private boolean active;
}