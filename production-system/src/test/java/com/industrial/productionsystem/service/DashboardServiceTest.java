package com.industrial.productionsystem.service;

import com.industrial.productionsystem.dto.DashboardResponse;
import com.industrial.productionsystem.entity.*;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.repository.MaquinaRepository;
import com.industrial.productionsystem.repository.OrdemDeProducaoRepository;
import com.industrial.productionsystem.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private MaquinaRepository maquinaRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private OrdemDeProducaoRepository ordemRepository;
    @InjectMocks private DashboardService service;

    private Company company;
    private Maquina maquinaAtiva;
    private Produto produto;
    private final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(COMPANY_ID);

        maquinaAtiva = new Maquina("Torno CNC", "CNC", 100, company);
        maquinaAtiva.setId(1L);
        maquinaAtiva.setStatus(StatusMaquina.ATIVA);

        produto = new Produto();
        produto.setId(10L);
        produto.setNome("Engrenagem");
        produto.setCompany(company);
    }

    private OrdemDeProducao criarOrdem(StatusOrdem status, int quantidade) {
        OrdemDeProducao o = new OrdemDeProducao();
        o.setProduto(produto);
        o.setMaquina(maquinaAtiva);
        o.setQuantidade(quantidade);
        o.setStatus(status);
        o.setCompany(company);
        return o;
    }

    @Test
    @DisplayName("Deve contar corretamente máquinas por status")
    void deveContarMaquinasPorStatus() {
        Maquina inativa = new Maquina("Fresadora", "Fresagem", 80, company);
        inativa.setStatus(StatusMaquina.INATIVA);
        Maquina manutencao = new Maquina("Torno X", "Torno", 60, company);
        manutencao.setStatus(StatusMaquina.MANUTENCAO);

        when(maquinaRepository.findByCompanyId(COMPANY_ID))
                .thenReturn(List.of(maquinaAtiva, inativa, manutencao));
        when(produtoRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());
        when(ordemRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(3, response.getTotalMaquinas());
        assertEquals(1, response.getMaquinasAtivas());
        assertEquals(1, response.getMaquinasInativas());
        assertEquals(1, response.getMaquinasEmManutencao());
    }

    @Test
    @DisplayName("Deve contar ordens por status e somar unidades produzidas")
    void deveContarOrdensPorStatusESomarUnidades() {
        when(maquinaRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());
        when(produtoRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());
        when(ordemRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(
                criarOrdem(StatusOrdem.PENDENTE, 50),
                criarOrdem(StatusOrdem.EM_PRODUCAO, 100),
                criarOrdem(StatusOrdem.FINALIZADA, 200),
                criarOrdem(StatusOrdem.FINALIZADA, 300)
        ));

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(4, response.getTotalOrdens());
        assertEquals(1, response.getOrdensPendentes());
        assertEquals(1, response.getOrdensEmProducao());
        assertEquals(2, response.getOrdensFinalizada());
        assertEquals(500, response.getTotalUnidadesProduzidas());    // 200 + 300
        assertEquals(150, response.getTotalUnidadesEmAberto());      // 50 + 100
    }

    @Test
    @DisplayName("Deve montar topMaquinas com a máquina mais produtiva primeiro")
    void deveOrdenarTopMaquinas() {
        Maquina maquina2 = new Maquina("Fresadora", "Fresagem", 80, company);
        maquina2.setId(2L);
        maquina2.setStatus(StatusMaquina.ATIVA);

        OrdemDeProducao o1 = criarOrdem(StatusOrdem.FINALIZADA, 100);
        OrdemDeProducao o2 = criarOrdem(StatusOrdem.FINALIZADA, 200);
        // Duas ordens na maquinaAtiva, uma na maquina2
        OrdemDeProducao o3 = new OrdemDeProducao();
        o3.setMaquina(maquina2);
        o3.setProduto(produto);
        o3.setQuantidade(50);
        o3.setStatus(StatusOrdem.FINALIZADA);
        o3.setCompany(company);

        when(maquinaRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(maquinaAtiva, maquina2));
        when(produtoRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(produto));
        when(ordemRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(o1, o2, o3));

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(2, response.getTopMaquinas().size());
        // Torno CNC tem 2 ordens, deve vir primeiro
        assertEquals("Torno CNC", response.getTopMaquinas().get(0).getMaquinaNome());
        assertEquals(2, response.getTopMaquinas().get(0).getOrdensFinalizadas());
        assertEquals(300, response.getTopMaquinas().get(0).getUnidadesProduzidas());
    }

    @Test
    @DisplayName("Deve retornar dashboard zerado quando empresa não tem dados")
    void deveRetornarDashboardZerado() {
        when(maquinaRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());
        when(produtoRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());
        when(ordemRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of());

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(0, response.getTotalMaquinas());
        assertEquals(0, response.getTotalOrdens());
        assertEquals(0, response.getTotalUnidadesProduzidas());
        assertTrue(response.getTopMaquinas().isEmpty());
    }
}