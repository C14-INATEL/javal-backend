package com.industrial.productionsystem.dto;

import com.industrial.productionsystem.entity.enums.SeveridadeFalha;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FalhaMaquinaRequest {

    @NotNull(message = "ID da máquina é obrigatório")
    private Long maquinaId;

    @NotBlank(message = "Descrição da falha é obrigatória")
    private String descricao;

    @NotNull(message = "Severidade é obrigatória")
    private SeveridadeFalha severidade;
}