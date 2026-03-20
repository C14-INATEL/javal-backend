package com.industrial.production_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompanyRegisterRequest {

    @NotBlank(message = "Nome da empresa é obrigatório")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    @NotBlank(message = "Nome do responsável é obrigatório")
    private String responsibleName;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;
}