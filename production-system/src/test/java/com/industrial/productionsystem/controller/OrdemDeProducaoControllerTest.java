package com.industrial.productionsystem.controller;

import com.industrial.productionsystem.entity.OrdemDeProducao;
import com.industrial.productionsystem.entity.enums.StatusOrdem;
import com.industrial.productionsystem.service.OrdemDeProducaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import java.util.List;
import org.junit.jupiter.api.DisplayName;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdemDeProducaoController.class)
@WithMockUser
class OrdemDeProducaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdemDeProducaoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /ordens - deve criar ordem de produção")
    void deveCriarOrdem() throws Exception {
        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setQuantidade(100);
        ordem.setStatus(StatusOrdem.PENDENTE);

        when(service.criar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ordem);

        mockMvc.perform(post("/ordens")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ordem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("GET /ordens - deve listar ordens de produção")
    void deveListarOrdens() throws Exception {
        when(service.listar()).thenReturn(List.of(new OrdemDeProducao()));

        mockMvc.perform(get("/ordens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("POST /ordens/{id}/iniciar - deve iniciar ordem de produção")
    void deveIniciarOrdem() throws Exception {
        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setStatus(StatusOrdem.EM_PRODUCAO);

        when(service.iniciar(1L)).thenReturn(ordem);

        mockMvc.perform(post("/ordens/1/iniciar")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PRODUCAO"));
    }

    @Test
    @DisplayName("POST /ordens/{id}/finalizar - deve finalizar ordem de produção")
    void deveFinalizarOrdem() throws Exception {
        OrdemDeProducao ordem = new OrdemDeProducao();
        ordem.setStatus(StatusOrdem.FINALIZADA);

        when(service.finalizar(1L)).thenReturn(ordem);

        mockMvc.perform(post("/ordens/1/finalizar")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));
    }
}