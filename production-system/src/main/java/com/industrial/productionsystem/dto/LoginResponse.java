package com.industrial.productionsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long companyId;
    private String companyName;
    private String email;
}