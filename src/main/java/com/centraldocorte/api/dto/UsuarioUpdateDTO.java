package com.centraldocorte.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "\\d{2}\\s?\\d{4,5}?\\d{4}", message = "Telefone inválido")
    private String telefone;
}
