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

    private OrdemDeProducao criarOrdem(Maquina maquina, StatusOrdem status, int quantidade) {
        OrdemDeProducao o = new OrdemDeProducao();
        o.setProduto(produto);
        o.setMaquina(maquina);
        o.setQuantidade(quantidade);
        o.setStatus(status);
        o.setCompany(company);
        return o;
    }

    private void mockMaquinas(long total, long ativas, long inativas, long manutencao) {
        when(maquinaRepository.countByCompanyId(COMPANY_ID)).thenReturn(total);
        when(maquinaRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusMaquina.ATIVA)).thenReturn(ativas);
        when(maquinaRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusMaquina.INATIVA)).thenReturn(inativas);
        when(maquinaRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusMaquina.MANUTENCAO)).thenReturn(manutencao);
    }

    private void mockOrdens(long total, long pendentes, long emProducao, long finalizadas,
                            long unidadesProduzidas, long unidadesEmAberto,
                            List<OrdemDeProducao> finalizadasComMaquina) {
        when(ordemRepository.countByCompanyId(COMPANY_ID)).thenReturn(total);
        when(ordemRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusOrdem.PENDENTE)).thenReturn(pendentes);
        when(ordemRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusOrdem.EM_PRODUCAO)).thenReturn(emProducao);
        when(ordemRepository.countByCompanyIdAndStatus(COMPANY_ID, StatusOrdem.FINALIZADA)).thenReturn(finalizadas);
        when(ordemRepository.sumQuantidadeByCompanyIdAndStatus(COMPANY_ID, StatusOrdem.FINALIZADA)).thenReturn(unidadesProduzidas);
        when(ordemRepository.sumQuantidadeByCompanyIdAndStatusIn(eq(COMPANY_ID), anyList())).thenReturn(unidadesEmAberto);
        when(ordemRepository.findFinalizadasComMaquina(COMPANY_ID)).thenReturn(finalizadasComMaquina);
    }

    @Test
    @DisplayName("Deve contar corretamente máquinas por status")
    void deveContarMaquinasPorStatus() {
        mockMaquinas(3, 1, 1, 1);
        when(produtoRepository.countByCompanyId(COMPANY_ID)).thenReturn(0L);
        mockOrdens(0, 0, 0, 0, 0, 0, List.of());

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(3, response.getTotalMaquinas());
        assertEquals(1, response.getMaquinasAtivas());
        assertEquals(1, response.getMaquinasInativas());
        assertEquals(1, response.getMaquinasEmManutencao());
    }

    @Test
    @DisplayName("Deve contar ordens por status e somar unidades produzidas")
    void deveContarOrdensPorStatusESomarUnidades() {
        mockMaquinas(0, 0, 0, 0);
        when(produtoRepository.countByCompanyId(COMPANY_ID)).thenReturn(0L);
        mockOrdens(4, 1, 1, 2, 500L, 150L, List.of());

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(4, response.getTotalOrdens());
        assertEquals(1, response.getOrdensPendentes());
        assertEquals(1, response.getOrdensEmProducao());
        assertEquals(2, response.getOrdensFinalizada());
        assertEquals(500, response.getTotalUnidadesProduzidas()); // 200 + 300
        assertEquals(150, response.getTotalUnidadesEmAberto());   // 50 + 100
    }

    @Test
    @DisplayName("Deve montar topMaquinas com a máquina mais produtiva primeiro")
    void deveOrdenarTopMaquinas() {
        Maquina maquina2 = new Maquina("Fresadora", "Fresagem", 80, company);
        maquina2.setId(2L);
        maquina2.setStatus(StatusMaquina.ATIVA);

        OrdemDeProducao o1 = criarOrdem(maquinaAtiva, StatusOrdem.FINALIZADA, 100);
        OrdemDeProducao o2 = criarOrdem(maquinaAtiva, StatusOrdem.FINALIZADA, 200);
        OrdemDeProducao o3 = criarOrdem(maquina2, StatusOrdem.FINALIZADA, 50);

        mockMaquinas(2, 2, 0, 0);
        when(produtoRepository.countByCompanyId(COMPANY_ID)).thenReturn(1L);
        mockOrdens(3, 0, 0, 3, 350L, 0L, List.of(o1, o2, o3));

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
        mockMaquinas(0, 0, 0, 0);
        when(produtoRepository.countByCompanyId(COMPANY_ID)).thenReturn(0L);
        mockOrdens(0, 0, 0, 0, 0L, 0L, List.of());

        DashboardResponse response = service.getDashboard(COMPANY_ID);

        assertEquals(0, response.getTotalMaquinas());
        assertEquals(0, response.getTotalOrdens());
        assertEquals(0, response.getTotalUnidadesProduzidas());
        assertTrue(response.getTopMaquinas().isEmpty());
    }
}