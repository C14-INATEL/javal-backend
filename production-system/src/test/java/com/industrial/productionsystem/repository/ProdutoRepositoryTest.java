package com.industrial.productionsystem.repository;

import java.util.List;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

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
    void deveSalvarProduto() {
        Produto p = new Produto();
        p.setNome("Produto A");
        p.setTempoProducaoUnitario(10);
        p.setCompany(company);

        Produto salvo = repository.save(p);

        assertNotNull(salvo.getId());
        assertEquals("Produto A", salvo.getNome());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirProdutos() {
        List<Produto> produtos = repository.findAll();

        assertTrue(produtos.isEmpty());
    }
}