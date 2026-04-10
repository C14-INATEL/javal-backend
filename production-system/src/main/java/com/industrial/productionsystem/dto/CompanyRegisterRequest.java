package com.industrial.productionsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CompanyRegisterRequest {

    @NotBlank(message = "Nome da empresa é obrigatório")
    private String name;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
            message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    private String cnpj;

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