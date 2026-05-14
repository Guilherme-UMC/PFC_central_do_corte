package com.example.crud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FuncionarioVinculoDTO {
    @NotBlank(message = "Email do funcionário é obrigatório")
    @Email(message = "Email deve ser válido")
    private String funcionarioEmail;
}