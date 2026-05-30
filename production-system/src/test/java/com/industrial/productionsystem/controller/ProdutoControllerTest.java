package com.industrial.productionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.productionsystem.dto.ProdutoRequest;
import com.industrial.productionsystem.dto.ProdutoResponse;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.security.JwtAuthFilter;
import com.industrial.productionsystem.service.ProdutoService;
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
        controllers = ProdutoController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@WithMockUser
class ProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private ProdutoService service;

    private org.springframework.security.core.Authentication mockAuth() {
        com.industrial.productionsystem.entity.Company company = new com.industrial.productionsystem.entity.Company();
        company.setId(1L);
        CompanyPrincipal principal = new CompanyPrincipal(company);
        return authenticated(principal, null, List.of());
    }

    private ProdutoResponse responseFixture(Long id, String nome) {
        ProdutoResponse r = new ProdutoResponse();
        r.setId(id);
        r.setNome(nome);
        r.setTempoProducaoUnitario(30);
        r.setCompanyId(1L);
        return r;
    }


    @Test
    @DisplayName("POST /api/produtos - deve criar produto e retornar 201")
    void deveCriarProduto() throws Exception {
        ProdutoRequest req = new ProdutoRequest();
        req.setNome("Engrenagem Industrial");
        req.setTempoProducaoUnitario(30);

        when(service.criar(any(ProdutoRequest.class), eq(1L)))
                .thenReturn(responseFixture(1L, "Engrenagem Industrial"));

        mockMvc.perform(post("/api/produtos")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Engrenagem Industrial"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    @DisplayName("POST /api/produtos - deve retornar 400 quando nome ausente")
    void deveRetornar400NomeAusente() throws Exception {
        ProdutoRequest req = new ProdutoRequest();
        req.setTempoProducaoUnitario(30);

        mockMvc.perform(post("/api/produtos")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any(), any());
    }


    @Test
    @DisplayName("GET /api/produtos - deve listar produtos da empresa")
    void deveListarProdutos() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(
                responseFixture(1L, "P1"),
                responseFixture(2L, "P2")
        ));

        mockMvc.perform(get("/api/produtos").with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    @DisplayName("DELETE /api/produtos/{id} - deve retornar 204")
    void deveDeletarProduto() throws Exception {
        doNothing().when(service).deletar(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/produtos/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }
}