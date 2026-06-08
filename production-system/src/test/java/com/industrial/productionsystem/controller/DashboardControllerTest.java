package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.dto.DashboardResponse;
import com.industrial.productionsystem.entity.Company;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    @Test
    void deveRetornarDashboardDaEmpresaAutenticada() {
        DashboardService dashboardService = mock(DashboardService.class);
        DashboardController controller = new DashboardController(dashboardService);

        Company company = Company.builder()
                .id(1L)
                .email("empresa@test.com")
                .name("Empresa Teste")
                .build();

        CompanyPrincipal principal = new CompanyPrincipal(company);

        DashboardResponse responseMock = DashboardResponse.builder()
                .totalMaquinas(2)
                .maquinasAtivas(1)
                .maquinasInativas(1)
                .maquinasEmManutencao(0)
                .totalProdutos(3)
                .totalOrdens(4)
                .ordensPendentes(1)
                .ordensEmProducao(1)
                .ordensFinalizada(2)
                .totalUnidadesProduzidas(500)
                .totalUnidadesEmAberto(150)
                .topMaquinas(List.of())
                .build();

        when(dashboardService.getDashboard(1L)).thenReturn(responseMock);

        ResponseEntity<DashboardResponse> response = controller.getDashboard(principal);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalMaquinas());
        assertEquals(3, response.getBody().getTotalProdutos());
        assertEquals(4, response.getBody().getTotalOrdens());
        assertEquals(500, response.getBody().getTotalUnidadesProduzidas());

        verify(dashboardService).getDashboard(1L);
    }
}