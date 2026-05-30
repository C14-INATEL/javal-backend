package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Maquina;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MaquinaRepositoryTest {

    @Autowired private MaquinaRepository maquinaRepository;
    @Autowired private CompanyRepository companyRepository;

    private Company company;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(Company.builder()
                .name("Empresa Teste")
                .cnpj("12345678000199")
                .email("teste@empresa.com")
                .phone("35999990000")
                .responsibleName("João")
                .password("hash")
                .build());
    }

    @Test
    @DisplayName("Deve salvar e recuperar máquina com companyId")
    void deveSalvarMaquinaComEmpresa() {
        Maquina m = new Maquina("Torno CNC", "CNC", 100, company);
        Maquina salva = maquinaRepository.save(m);

        assertNotNull(salva.getId());
        assertEquals("Torno CNC", salva.getNome());
        assertEquals(company.getId(), salva.getCompany().getId());
        assertEquals(StatusMaquina.ATIVA, salva.getStatus());
    }

    @Test
    @DisplayName("findByCompanyId deve retornar apenas máquinas da empresa")
    void deveFiltrarPorEmpresa() {
        maquinaRepository.save(new Maquina("M1", "CNC", 100, company));
        maquinaRepository.save(new Maquina("M2", "Laser", 200, company));

        List<Maquina> lista = maquinaRepository.findByCompanyId(company.getId());

        assertEquals(2, lista.size());
        lista.forEach(m -> assertEquals(company.getId(), m.getCompany().getId()));
    }

    @Test
    @DisplayName("findByIdAndCompanyId deve retornar presente quando IDs batem")
    void deveEncontrarPorIdEEmpresa() {
        Maquina salva = maquinaRepository.save(new Maquina("M1", "CNC", 100, company));

        Optional<Maquina> result = maquinaRepository.findByIdAndCompanyId(salva.getId(), company.getId());

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findByIdAndCompanyId deve retornar vazio quando empresa não bate")
    void deveRetornarVazioQuandoEmpresaDiverge() {
        Maquina salva = maquinaRepository.save(new Maquina("M1", "CNC", 100, company));

        Optional<Maquina> result = maquinaRepository.findByIdAndCompanyId(salva.getId(), 999L);

        assertTrue(result.isEmpty());
    }
}
