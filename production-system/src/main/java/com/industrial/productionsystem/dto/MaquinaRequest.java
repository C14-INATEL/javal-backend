package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.enums.StatusMaquina;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaquinaRequest {

    @NotBlank(message = "Nome da máquina é obrigatório")
    private String nome;

    @NotBlank(message = "Tipo da máquina é obrigatório")
    private String tipo;

    @NotNull(message = "Capacidade por hora é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    private Integer capacidadePorHora;

    private StatusMaquina status; // opcional — padrão ATIVA se nulo
}