package com.industrial.productionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.productionsystem.dto.FalhaMaquinaRequest;
import com.industrial.productionsystem.dto.FalhaMaquinaResponse;
import com.industrial.productionsystem.entity.enums.SeveridadeFalha;
import com.industrial.productionsystem.entity.enums.StatusFalha;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.security.JwtAuthFilter;
import com.industrial.productionsystem.service.FalhaMaquinaService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FalhaMaquinaController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@WithMockUser
class FalhaMaquinaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private FalhaMaquinaService service;

    private FalhaMaquinaResponse responseFixture(Long id, StatusFalha status) {
        FalhaMaquinaResponse r = new FalhaMaquinaResponse();
        r.setId(id);
        r.setDescricao("Motor superaquecendo");
        r.setSeveridade(SeveridadeFalha.ALTA);
        r.setStatus(status);
        r.setDataAbertura(LocalDateTime.now());
        r.setMaquinaId(10L);
        r.setMaquinaNome("Torno CNC 01");
        r.setCompanyId(1L);
        return r;
    }

    private org.springframework.security.core.Authentication mockAuth() {
        com.industrial.productionsystem.entity.Company company =
                new com.industrial.productionsystem.entity.Company();
        company.setId(1L);
        CompanyPrincipal principal = new CompanyPrincipal(company);
        return authenticated(principal, null, List.of());
    }

    @Test
    @DisplayName("POST /api/falhas - deve retornar 201 com a falha registrada")
    void deveRegistrarFalha() throws Exception {
        FalhaMaquinaRequest req = new FalhaMaquinaRequest();
        req.setMaquinaId(10L);
        req.setDescricao("Motor superaquecendo");
        req.setSeveridade(SeveridadeFalha.ALTA);

        when(service.registrar(any(FalhaMaquinaRequest.class), eq(1L)))
                .thenReturn(responseFixture(100L, StatusFalha.ABERTA));

        mockMvc.perform(post("/api/falhas")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.maquinaNome").value("Torno CNC 01"));
    }

    @Test
    @DisplayName("POST /api/falhas - deve retornar 400 quando descrição ausente")
    void deveRetornar400DescricaoAusente() throws Exception {
        FalhaMaquinaRequest req = new FalhaMaquinaRequest();
        req.setMaquinaId(10L);
        req.setSeveridade(SeveridadeFalha.ALTA);

        mockMvc.perform(post("/api/falhas")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).registrar(any(), any());
    }

    @Test
    @DisplayName("POST /api/falhas - deve retornar 400 quando severidade ausente")
    void deveRetornar400SeveridadeAusente() throws Exception {
        FalhaMaquinaRequest req = new FalhaMaquinaRequest();
        req.setMaquinaId(10L);
        req.setDescricao("Motor superaquecendo");

        mockMvc.perform(post("/api/falhas")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/falhas - deve listar falhas da empresa")
    void deveListarFalhas() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(
                responseFixture(100L, StatusFalha.ABERTA),
                responseFixture(101L, StatusFalha.RESOLVIDA)
        ));

        mockMvc.perform(get("/api/falhas")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/falhas/maquina/{id} - deve listar o histórico da máquina")
    void deveListarFalhasPorMaquina() throws Exception {
        when(service.listarPorMaquina(eq(10L), eq(1L)))
                .thenReturn(List.of(responseFixture(100L, StatusFalha.RESOLVIDA)));

        mockMvc.perform(get("/api/falhas/maquina/10")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].maquinaId").value(10));
    }

    @Test
    @DisplayName("PATCH /api/falhas/{id}/resolver - deve resolver a falha")
    void deveResolverFalha() throws Exception {
        when(service.resolver(eq(100L), eq(1L)))
                .thenReturn(responseFixture(100L, StatusFalha.RESOLVIDA));

        mockMvc.perform(patch("/api/falhas/100/resolver")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDA"));
    }
}