package com.industrial.productionsystem.service;

import com.industrial.productionsystem.exception.NotFoundException;
import com.industrial.productionsystem.dto.OrdemRequest;
import com.industrial.productionsystem.dto.OrdemResponse;
import com.industrial.productionsystem.entity.*;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemDeProducaoServiceTest {

    @Mock private OrdemDeProducaoRepository repository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private MaquinaRepository maquinaRepository;
    @Mock private CompanyRepository companyRepository;
    @InjectMocks private OrdemDeProducaoService service;

    private Company company;
    private Produto produto;
    private Maquina maquina;
    private final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(COMPANY_ID);

        produto = new Produto();
        produto.setId(10L);
        produto.setNome("Engrenagem");
        produto.setTempoProducaoUnitario(30);
        produto.setCompany(company);

        maquina = new Maquina("Torno CNC", "CNC", 100, company);
        maquina.setStatus(StatusMaquina.ATIVA);
    }

    // ── helper ──────────────────────────────────────────────────────

    private OrdemDeProducao ordemSalva(StatusOrdem status) {
        OrdemDeProducao o = new OrdemDeProducao();
        o.setId(1L);
        o.setProduto(produto);
        o.setMaquina(maquina);
        o.setQuantidade(200);
        o.setStatus(status);
        o.setCompany(company);
        return o;
    }

    private OrdemRequest requestValido() {
        OrdemRequest req = new OrdemRequest();
        req.setProdutoId(10L);
        req.setMaquinaId(5L);
        req.setQuantidade(200);
        return req;
    }

    // ── criar ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar ordem com status PENDENTE independente do que for enviado")
    void deveCriarOrdemComStatusPendente() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(produtoRepository.findByIdAndCompanyId(10L, COMPANY_ID)).thenReturn(Optional.of(produto));
        when(maquinaRepository.findByIdAndCompanyId(5L, COMPANY_ID)).thenReturn(Optional.of(maquina));
        when(repository.save(any())).thenReturn(ordemSalva(StatusOrdem.PENDENTE));

        OrdemResponse response = service.criar(requestValido(), COMPANY_ID);

        assertEquals(StatusOrdem.PENDENTE, response.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Não deve criar ordem com máquina INATIVA")
    void naoDeveCriarOrdemComMaquinaInativa() {
        maquina.setStatus(StatusMaquina.INATIVA);

        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(produtoRepository.findByIdAndCompanyId(10L, COMPANY_ID)).thenReturn(Optional.of(produto));
        when(maquinaRepository.findByIdAndCompanyId(5L, COMPANY_ID)).thenReturn(Optional.of(maquina));

        assertThrows(IllegalArgumentException.class,
                () -> service.criar(requestValido(), COMPANY_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando produto não pertence à empresa")
    void deveLancarExcecaoProdutoDeOutraEmpresa() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(produtoRepository.findByIdAndCompanyId(10L, COMPANY_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.criar(requestValido(), COMPANY_ID));

        assertEquals("Produto não encontrado nesta empresa", exception.getMessage());
        verify(repository, never()).save(any());
    }

    // ── iniciar ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve iniciar ordem PENDENTE com máquina ATIVA")
    void deveIniciarOrdemPendente() {
        OrdemDeProducao ordem = ordemSalva(StatusOrdem.PENDENTE);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(ordem));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemResponse response = service.iniciar(1L, COMPANY_ID);

        assertEquals(StatusOrdem.EM_PRODUCAO, response.getStatus());
        assertNotNull(response.getDataInicio());
    }

    @Test
    @DisplayName("Não deve iniciar ordem que não está PENDENTE")
    void naoDeveIniciarOrdemJaEmProducao() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(ordemSalva(StatusOrdem.EM_PRODUCAO)));

        assertThrows(IllegalArgumentException.class,
                () -> service.iniciar(1L, COMPANY_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve iniciar ordem com máquina em MANUTENCAO")
    void naoDeveIniciarOrdemComMaquinaEmManutencao() {
        maquina.setStatus(StatusMaquina.MANUTENCAO);
        OrdemDeProducao ordem = ordemSalva(StatusOrdem.PENDENTE);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(ordem));

        assertThrows(IllegalArgumentException.class,
                () -> service.iniciar(1L, COMPANY_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve definir dataInicio ao iniciar ordem")
    void deveDefinirDataInicioAoIniciar() {
        OrdemDeProducao ordem = ordemSalva(StatusOrdem.PENDENTE);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(ordem));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemResponse response = service.iniciar(1L, COMPANY_ID);

        assertNotNull(response.getDataInicio());
    }

    // ── finalizar ────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve finalizar ordem EM_PRODUCAO")
    void deveFinalizarOrdem() {
        OrdemDeProducao ordem = ordemSalva(StatusOrdem.EM_PRODUCAO);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(ordem));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemResponse response = service.finalizar(1L, COMPANY_ID);

        assertEquals(StatusOrdem.FINALIZADA, response.getStatus());
        assertNotNull(response.getDataFim());
    }

    @Test
    @DisplayName("Não deve finalizar ordem que não está EM_PRODUCAO")
    void naoDeveFinalizarOrdemPendente() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(ordemSalva(StatusOrdem.PENDENTE)));

        assertThrows(IllegalArgumentException.class,
                () -> service.finalizar(1L, COMPANY_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao iniciar ordem inexistente")
    void deveLancarExcecaoOrdemInexistente() {
        when(repository.findByIdAndCompanyId(99L, COMPANY_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.iniciar(99L, COMPANY_ID));

        assertEquals("Ordem não encontrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar ordem PENDENTE")
    void deveCancelarOrdemPendente() {
        OrdemDeProducao ordem = ordemSalva(StatusOrdem.PENDENTE);

        when(repository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(ordem));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemResponse response = service.cancelar(1L, COMPANY_ID);

        assertEquals(StatusOrdem.CANCELADA, response.getStatus());
        assertNotNull(response.getDataFim());
    }

    @Test
    @DisplayName("Não deve cancelar ordem EM_PRODUCAO")
    void naoDeveCancelarOrdemEmProducao() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(ordemSalva(StatusOrdem.EM_PRODUCAO)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelar(1L, COMPANY_ID)
        );

        assertEquals("Apenas ordens PENDENTES podem ser canceladas", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve cancelar ordem FINALIZADA")
    void naoDeveCancelarOrdemFinalizada() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(ordemSalva(StatusOrdem.FINALIZADA)));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelar(1L, COMPANY_ID));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve cancelar ordem já CANCELADA")
    void naoDeveCancelarOrdemJaCancelada() {
        when(repository.findByIdAndCompanyId(1L, COMPANY_ID))
                .thenReturn(Optional.of(ordemSalva(StatusOrdem.CANCELADA)));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelar(1L, COMPANY_ID));

        verify(repository, never()).save(any());
    }
}
