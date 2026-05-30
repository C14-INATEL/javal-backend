package com.industrial.productionsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.productionsystem.dto.MaquinaRequest;
import com.industrial.productionsystem.dto.MaquinaResponse;
import com.industrial.productionsystem.entity.enums.StatusMaquina;
import com.industrial.productionsystem.security.CompanyPrincipal;
import com.industrial.productionsystem.security.JwtAuthFilter;
import com.industrial.productionsystem.service.MaquinaService;
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
        controllers = MaquinaController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
@WithMockUser
class MaquinaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private MaquinaService service;


    private MaquinaResponse responseFixture(Long id, String nome, StatusMaquina status) {
        MaquinaResponse r = new MaquinaResponse();
        r.setId(id);
        r.setNome(nome);
        r.setTipo("CNC");
        r.setCapacidadePorHora(100);
        r.setStatus(status);
        r.setCompanyId(1L);
        return r;
    }

    private org.springframework.security.core.Authentication mockAuth() {
        com.industrial.productionsystem.entity.Company company = new com.industrial.productionsystem.entity.Company();
        company.setId(1L);
        CompanyPrincipal principal = new CompanyPrincipal(company);
        return authenticated(principal, null, List.of());
    }


    @Test
    @DisplayName("POST /api/maquinas - deve retornar 201 com máquina criada")
    void deveCriarMaquina() throws Exception {
        MaquinaRequest req = new MaquinaRequest();
        req.setNome("Torno CNC 01");
        req.setTipo("CNC");
        req.setCapacidadePorHora(100);

        when(service.criar(any(MaquinaRequest.class), eq(1L)))
                .thenReturn(responseFixture(1L, "Torno CNC 01", StatusMaquina.ATIVA));

        mockMvc.perform(post("/api/maquinas")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Torno CNC 01"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    @DisplayName("POST /api/maquinas - deve retornar 400 quando nome ausente")
    void deveRetornar400NomeAusente() throws Exception {
        MaquinaRequest req = new MaquinaRequest();
        req.setTipo("CNC");
        req.setCapacidadePorHora(100);

        mockMvc.perform(post("/api/maquinas")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any(), any());
    }


    @Test
    @DisplayName("GET /api/maquinas - deve listar máquinas da empresa")
    void deveListarMaquinas() throws Exception {
        when(service.listar(1L)).thenReturn(List.of(
                responseFixture(1L, "M1", StatusMaquina.ATIVA),
                responseFixture(2L, "M2", StatusMaquina.INATIVA)
        ));

        mockMvc.perform(get("/api/maquinas")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    @DisplayName("PATCH /api/maquinas/{id}/status - deve alterar status")
    void deveAlterarStatus() throws Exception {
        when(service.alterarStatus(eq(1L), eq(StatusMaquina.MANUTENCAO), eq(1L)))
                .thenReturn(responseFixture(1L, "Torno", StatusMaquina.MANUTENCAO));

        mockMvc.perform(patch("/api/maquinas/1/status")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .param("status", "MANUTENCAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MANUTENCAO"));
    }


    @Test
    @DisplayName("DELETE /api/maquinas/{id} - deve retornar 204")
    void deveDeletarMaquina() throws Exception {
        doNothing().when(service).deletar(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/maquinas/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }
}
