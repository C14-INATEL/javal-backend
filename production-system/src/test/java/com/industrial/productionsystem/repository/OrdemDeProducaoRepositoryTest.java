package com.industrial.productionsystem.repository;

import com.industrial.productionsystem.entity.*;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
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
class OrdemDeProducaoRepositoryTest {

    @Autowired private OrdemDeProducaoRepository ordemRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private MaquinaRepository maquinaRepository;

    private Company company;
    private Produto produto;
    private Maquina maquina;

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

        produto = new Produto();
        produto.setNome("Engrenagem");
        produto.setTempoProducaoUnitario(30);
        produto.setCompany(company);
        produto = produtoRepository.save(produto);

        maquina = maquinaRepository.save(new Maquina("Torno CNC", "CNC", 100, company));
    }

    private OrdemDeProducao salvarOrdem(StatusOrdem status) {
        OrdemDeProducao o = new OrdemDeProducao();
        o.setProduto(produto);
        o.setMaquina(maquina);
        o.setQuantidade(100);
        o.setStatus(status);
        o.setCompany(company);
        return ordemRepository.save(o);
    }

    @Test
    @DisplayName("Deve salvar ordem com company, produto e máquina")
    void deveSalvarOrdem() {
        OrdemDeProducao salva = salvarOrdem(StatusOrdem.PENDENTE);

        assertNotNull(salva.getId());
        assertEquals(StatusOrdem.PENDENTE, salva.getStatus());
        assertEquals(company.getId(), salva.getCompany().getId());
    }

    @Test
    @DisplayName("findByCompanyId deve retornar ordens da empresa")
    void deveFiltrarPorEmpresa() {
        salvarOrdem(StatusOrdem.PENDENTE);
        salvarOrdem(StatusOrdem.EM_PRODUCAO);

        List<OrdemDeProducao> lista = ordemRepository.findByCompanyId(company.getId());

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("findByIdAndCompanyId deve retornar presente quando IDs batem")
    void deveEncontrarPorIdEEmpresa() {
        OrdemDeProducao salva = salvarOrdem(StatusOrdem.PENDENTE);

        Optional<OrdemDeProducao> result =
                ordemRepository.findByIdAndCompanyId(salva.getId(), company.getId());

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findByMaquinaIdAndCompanyId deve retornar ordens da máquina")
    void deveFiltrarPorMaquinaEEmpresa() {
        salvarOrdem(StatusOrdem.PENDENTE);
        salvarOrdem(StatusOrdem.FINALIZADA);

        List<OrdemDeProducao> lista =
                ordemRepository.findByMaquinaIdAndCompanyId(maquina.getId(), company.getId());

        assertEquals(2, lista.size());
    }
}
