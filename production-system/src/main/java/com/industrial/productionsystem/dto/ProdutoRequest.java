package com.industrial.productionsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProdutoRequest {

    @NotBlank(message = "Nome do produto é obrigatório")
    private String nome;

    @NotNull(message = "Tempo de produção unitário é obrigatório")
    @Min(value = 1, message = "Tempo de produção deve ser maior que zero")
    private Integer tempoProducaoUnitario;
}