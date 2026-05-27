package com.industrial.productionsystem.security;

import com.industrial.productionsystem.entity.Company;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CompanyPrincipal {
    private final Company company;

    public Long getId() {
        return company.getId();
    }

    public String getEmail() {
        return company.getEmail();
    }
}