package com.industrial.productionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.productionsystem.dto.OrdemRequest;
import com.industrial.productionsystem.dto.OrdemResponse;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.security.JwtAuthFilter;
import com.industrial.productionsystem.service.OrdemDeProducaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = OrdemDeProducaoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@WithMockUser
class OrdemDeProducaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private OrdemDeProducaoService service;

    private org.springframework.security.core.Authentication mockAuth() {
        com.industrial.productionsystem.entity.Company company = new com.industrial.productionsystem.entity.Company();
        company.setId(1L);
        CompanyPrincipal principal = new CompanyPrincipal(company);
        return authenticated(principal, null, List.of());
    }

    private OrdemResponse ordemResponse(StatusOrdem status) {
        OrdemResponse r = new OrdemResponse();
        r.setId(1L);
        r.setProdutoId(10L);
        r.setProdutoNome("Engrenagem");
        r.setMaquinaId(5L);
        r.setMaquinaNome("Torno CNC");
        r.setQuantidade(200);
        r.setStatus(status);
        r.setCompanyId(1L);
        return r;
    }


    @Test
    @DisplayName("POST /api/ordens - deve criar ordem PENDENTE e retornar 201")
    void deveCriarOrdem() throws Exception {
        OrdemRequest req = new OrdemRequest();
        req.setProdutoId(10L);
        req.setMaquinaId(5L);
        req.setQuantidade(200);

        when(service.criar(any(OrdemRequest.class), eq(1L)))
                .thenReturn(ordemResponse(StatusOrdem.PENDENTE));

        mockMvc.perform(post("/api/ordens")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.quantidade").value(200));
    }

    @Test
    @DisplayName("POST /api/ordens - deve retornar 400 com quantidade nula")
    void deveRetornar400QuantidadeNula() throws Exception {
        OrdemRequest req = new OrdemRequest();
        req.setProdutoId(10L);
        req.setMaquinaId(5L);

        mockMvc.perform(post("/api/ordens")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any(), any());
    }


    @Test
    @DisplayName("GET /api/ordens - deve listar ordens da empresa")
    void deveListarOrdens() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(
                ordemResponse(StatusOrdem.PENDENTE),
                ordemResponse(StatusOrdem.EM_PRODUCAO)
        ));

        mockMvc.perform(get("/api/ordens").with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    @DisplayName("POST /api/ordens/{id}/iniciar - deve retornar ordem EM_PRODUCAO")
    void deveIniciarOrdem() throws Exception {
        when(service.iniciar(eq(1L), eq(1L)))
                .thenReturn(ordemResponse(StatusOrdem.EM_PRODUCAO));

        mockMvc.perform(post("/api/ordens/1/iniciar")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PRODUCAO"));
    }


    @Test
    @DisplayName("POST /api/ordens/{id}/finalizar - deve retornar ordem FINALIZADA")
    void deveFinalizarOrdem() throws Exception {
        when(service.finalizar(eq(1L), eq(1L)))
                .thenReturn(ordemResponse(StatusOrdem.FINALIZADA));

        mockMvc.perform(post("/api/ordens/1/finalizar")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));
    }
}
