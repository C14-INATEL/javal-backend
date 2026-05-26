package com.industrial.productionsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrdemRequest {

    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "Máquina é obrigatória")
    private Long maquinaId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
}