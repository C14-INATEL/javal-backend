package com.industrial.productionsystem.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    void deveCriarCompanyComBuilder() {
        Company company = Company.builder()
                .name("Empresa Teste")
                .email("teste@email.com")
                .cnpj("123456789")
                .phone("999999999")
                .responsibleName("João")
                .password("123")
                .build();

        assertEquals("Empresa Teste", company.getName());
        assertEquals("teste@email.com", company.getEmail());
    }
}