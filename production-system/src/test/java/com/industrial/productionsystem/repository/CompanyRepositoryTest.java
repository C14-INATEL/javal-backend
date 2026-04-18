package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Company;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository repository;

    @Test
    void deveVerificarSeEmailExiste() {
        Company company = Company.builder()
                .name("Empresa")
                .email("teste@email.com")
                .cnpj("123")
                .phone("999")
                .responsibleName("João")
                .password("123")
                .build();

        repository.save(company);

        boolean existe = repository.existsByEmail("teste@email.com");

        assertTrue(existe);
    }

    @Test
    void deveVerificarSeCnpjExiste() {
        Company company = Company.builder()
                .name("Empresa")
                .email("teste@email.com")
                .cnpj("123456")
                .phone("999")
                .responsibleName("João")
                .password("123")
                .build();

        repository.save(company);

        boolean existe = repository.existsByCnpj("123456");

        assertTrue(existe);
    }
}