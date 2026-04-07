package com.industrial.production_system.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CompanyRegisterResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String responsibleName;
    private LocalDateTime createdAt;
}