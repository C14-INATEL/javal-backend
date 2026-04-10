package com.industrial.productionsystem.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
public class CompanyRegisterResponse {
    private Long id;
    private String name;
    private String cnpj;
    private String email;
    private String phone;
    private String responsibleName;
    private LocalDateTime createdAt;
}