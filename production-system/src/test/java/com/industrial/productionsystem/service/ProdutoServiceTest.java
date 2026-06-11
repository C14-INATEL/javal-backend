package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.ProdutoRequest;
import com.industrial.productionsystem.dto.ProdutoResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.entity.Produto;
import com.industrial.productionsystem.repository.CompanyRepository;
import com.industrial.productionsystem.repository.ProdutoRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock private ProdutoRepository repository;
    @Mock private CompanyRepository companyRepository;
    @Mock private OrdemDeProducaoRepository ordemDeProducaoRepository;
    @InjectMocks private ProdutoService service;

    private Company company;
    private final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(COMPANY_ID);
        company.setName("Empresa Teste");
    }

    private ProdutoRequest requestValido() {
        ProdutoRequest req = new ProdutoRequest();
        req.setNome("Engrenagem Industrial");
        req.setTempoProducaoUnitario(30);
        return req;
    }

    private Produto produtoSalvo(String nome) {
        Produto p = new Produto();
        p.setId(1L);
        p.setNome(nome);
        p.setTempoProducaoUnitario(30);
        p.setCompany(company);
        return p;
    }

    // ── criar ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void deveCriarProduto() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(repository.save(any())).thenReturn(produtoSalvo("Engrenagem Industrial"));

        ProdutoResponse response = service.criar(requestValido(), COMPANY_ID);

        assertNotNull(response);
        assertEquals("Engrenagem Industrial", response.getNome());
        assertEquals(COMPANY_ID, response.getCompanyId());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto para empresa inexistente")
    void deveLancarExcecaoEmpresaInexistente() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.criar(requestValido(), COMPANY_ID));
        verify(repository, never()).save(any());
    }

    // ── listar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar apenas produtos da empresa")
    void deveListarProdutosDaEmpresa() {
        when(repository.findByCompanyId(COMPANY_ID))
                .thenReturn(List.of(produtoSalvo("P1"), produtoSalvo("P2")));

        List<ProdutoResponse> lista = service.listar(COMPANY_ID);

        assertEquals(2, lista.size());
        verify(repository, times(1)).findByCompanyId(COMPANY_ID);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando empresa não tem produtos")
    void deveRetornarListaVazia() {
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());

        List<ProdutoResponse> lista = service.listar(COMPANY_ID);

        assertTrue(lista.isEmpty());
    }

    // ── deletar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void deveDeletarProduto() {

        Produto produto = produtoSalvo("P1");

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(produto));

        when(ordemDeProducaoRepository.existsByProdutoIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(false);

        service.deletar(1L, COMPANY_ID);

        verify(repository, times(1)).delete(produto);
    }

    @Test
    @DisplayName("Não deve deletar produto com ordens vinculadas")
    void naoDeveDeletarProdutoComOrdensVinculadas() {

        Produto produto = produtoSalvo("P1");

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(produto));

        when(ordemDeProducaoRepository.existsByProdutoIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletar(1L, COMPANY_ID)
        );

        assertEquals(
                "Não é possível excluir produto com ordens vinculadas",
                exception.getMessage()
        );

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto de outra empresa")
    void deveLancarExcecaoAoDeletarProdutoDeOutraEmpresa() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.deletar(1L, COMPANY_ID));
        verify(repository, never()).delete(any());
    }
}