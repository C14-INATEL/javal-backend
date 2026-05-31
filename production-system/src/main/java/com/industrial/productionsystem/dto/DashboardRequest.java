package com.industrial.productionsystem.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DashboardRequest {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Long linhaProducaoId;
}